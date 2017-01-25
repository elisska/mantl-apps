package com.cisco.mantl.integration;


import kafka.admin.TopicCommand;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.message.MessageAndMetadata;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringDecoder;
import kafka.serializer.StringEncoder;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.VerifiableProperties;

import org.junit.ComparisonFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by dbort on 16.10.2015.
 */
public class KafkaUnit {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaUnit.class);

    private KafkaServerStartable broker;
    private Zookeeper zookeeper;
    private final String zookeeperString;
    private final String brokerString;
    private int zkPort;
    private int brokerPort;

    public int getBrokerPort() {
        return brokerPort;
    }

    public String getBrokerString() {
        return brokerString;
    }

    public KafkaUnit(int zkPort, int brokerPort) {
        this.zkPort = zkPort;
        this.brokerPort = brokerPort;
        this.zookeeperString = "localhost:" + zkPort;
        this.brokerString = "localhost:" + brokerPort;
    }

    public void startup() {

        zookeeper = new Zookeeper(zkPort);
        zookeeper.startup();

        File logDir;
        try {
            logDir = Files.createTempDirectory("kafka").toFile();
        } catch (IOException e) {
            throw new RuntimeException("Unable to start Kafka", e);
        }
        logDir.deleteOnExit();
        Properties properties = new Properties();
        properties.setProperty("zookeeper.connect", zookeeperString);
        properties.setProperty("broker.id", "1");
        properties.setProperty("host.name", "localhost");
        properties.setProperty("port", Integer.toString(brokerPort));
        properties.setProperty("log.dir", logDir.getAbsolutePath());
        properties.setProperty("log.flush.interval.messages", String.valueOf(1));

        broker = new KafkaServerStartable(new KafkaConfig(properties));
        broker.startup();
    }

    public void createTopic(String topicName) {
        String[] arguments = new String[9];
        arguments[0] = "--create";
        arguments[1] = "--zookeeper";
        arguments[2] = zookeeperString;
        arguments[3] = "--replication-factor";
        arguments[4] = "1";
        arguments[5] = "--partitions";
        arguments[6] = "1";
        arguments[7] = "--topic";
        arguments[8] = topicName;
        LOGGER.info("Executing: CreateTopic " + Arrays.toString(arguments));
        TopicCommand.main(arguments);
    }


    public void shutdown() {
        if (broker != null) broker.shutdown();
        if (zookeeper != null) zookeeper.shutdown();
    }

    public List<String> readMessages(String topicName, final int expectedMessages) throws TimeoutException {
        ExecutorService singleThread = Executors.newSingleThreadExecutor();
        Properties consumerProperties = new Properties();
        consumerProperties.put("zookeeper.connect", zookeeperString);
        consumerProperties.put("group.id", "10");
        consumerProperties.put("socket.timeout.ms", "500");
        consumerProperties.put("consumer.id", "test");
        consumerProperties.put("auto.offset.reset", "smallest");
        consumerProperties.put("consumer.timeout.ms", "500");
        ConsumerConnector javaConsumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(consumerProperties));
        StringDecoder stringDecoder = new StringDecoder(new VerifiableProperties(new Properties()));
        Map<String, Integer> topicMap = new HashMap<String, Integer>();
        topicMap.put(topicName, 1);
        Map<String, List<KafkaStream<String, String>>> events = javaConsumerConnector.createMessageStreams(topicMap, stringDecoder, stringDecoder);
        List<KafkaStream<String, String>> events1 = events.get(topicName);
        final KafkaStream<String, String> kafkaStreams = events1.get(0);


        Future<List<String>> submit = singleThread.submit(new Callable<List<String>>() {
            public List<String> call() throws Exception {
                List<String> messages = new ArrayList<String>();
                try {
                    for (MessageAndMetadata<String, String> kafkaStream : kafkaStreams) {
                        String message = kafkaStream.message();
                        LOGGER.info("Received message: {}", message);
                        messages.add(message);
                    }
                } catch (ConsumerTimeoutException e) {
                    // always gets throws reaching the end of the stream
                }
                if (messages.size() != expectedMessages) {
                    throw new ComparisonFailure("Incorrect number of messages returned", Integer.toString(expectedMessages),
                            Integer.toString(messages.size()));
                }
                return messages;
            }
        });

        try {
            return submit.get(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            if (e.getCause() instanceof ComparisonFailure) {
                throw (ComparisonFailure) e.getCause();
            }
            throw new TimeoutException("Timed out waiting for messages");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ComparisonFailure) {
                throw (ComparisonFailure) e.getCause();
            }
            throw new TimeoutException("Timed out waiting for messages");
        } catch (TimeoutException e) {
            if (e.getCause() instanceof ComparisonFailure) {
                throw (ComparisonFailure) e.getCause();
            }
            throw new TimeoutException("Timed out waiting for messages");
        } finally {
            singleThread.shutdown();
            javaConsumerConnector.shutdown();
        }
    }

    @SafeVarargs
    public final void sendMessages(KeyedMessage<String, String> message, KeyedMessage<String, String>... messages) {
        Properties props = new Properties();
        props.put("serializer.class", StringEncoder.class.getName());
        props.put("metadata.broker.list", brokerString);
        ProducerConfig config = new ProducerConfig(props);
        Producer<String, String> producer = new Producer<String, String>(config);
        producer.send(message);
        producer.send(Arrays.asList(messages));

    }
}
