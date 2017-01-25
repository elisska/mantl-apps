This demo instructions should be applicable to any CDH cluster.

## Prerequisites

Below steps are based on these assumptions:

1) User's home directory on HDFS is `/user/$USER` where `$USER` is the current user logged on to the gateway host.
In case different directory is planned to be used, make sure write permission is available.

2) Input dataset is in `hdfs:///data/sasa-logs-without-recursion` directory. Recursive directory read IS SUPPORTED for the application, but here we use directory that does not contain recutsive folders ( since previously we demonstrate file aggregation using  [File aggregator](https://github.com/CiscoCloud/mantl-apps/blob/master/useful-apps/spark-useful-apps/file-aggregator) application )

3) Zookeeper quorum is: `chevron-virginia-1-hs3-mini-master-03:2181,chevron-virginia-1-hs3-mini-master-02:2181,chevron-virginia-1-hs3-mini-master-01:2181`

4) Kafka brokers are: `chevron-virginia-1-hs3-mini-master-01:9092,chevron-virginia-1-hs3-mini-master-02:9092`

* Login to a gateway host and run the below commands:

```
git clone https://github.com/CiscoCloud/mantl-apps
cd mantl-apps/demo-apps/hadoop-demo-apps/fs-kafka-producer
mvn clean install -Pprod
ls -al target/kafka_producer-1.0.jar
```

* *Optional step:* create and configure Kafka topic if required:
```
/opt/cloudera/parcels/KAFKA/bin/kafka-topics --create --zookeeper chevron-virginia-1-hs3-mini-master-03:2181,chevron-virginia-1-hs3-mini-master-02:2181,chevron-virginia-1-hs3-mini-master-01:2181 --replication-factor 1 --partitions 3 --topic test-topic
```

```
 /opt/cloudera/parcels/KAFKA/bin/kafka-topics --describe --zookeeper chevron-virginia-1-hs3-mini-master-03:2181,chevron-virginia-1-hs3-mini-master-02:2181,chevron-virginia-1-hs3-mini-master-01:2181 --topic test-topic
 ```

## Run Kafka Producer application

* Start application (as a background process):

```
nohup hadoop jar target/kafka_producer-1.0.jar com.cisco.mantl.kafka.KProdDriver --brokers chevron-virginia-1-hs3-mini-master-01:9092,chevron-virginia-1-hs3-mini-master-02:9092 --topic test-topic --inputDir hdfs:///data/sasa-logs-without-recursion --zkhost chevron-virginia-1-hs3-mini-master-03:2181,chevron-virginia-1-hs3-mini-master-02:2181,chevron-virginia-1-hs3-mini-master-01:2181 &
```

* Optional step: start Kafka consumer to verify that messages are going to the topic:
```
/opt/cloudera/parcels/KAFKA/bin/kafka-console-consumer --zookeeper chevron-virginia-1-hs3-mini-master-03:2181,chevron-virginia-1-hs3-mini-master-02:2181,chevron-virginia-1-hs3-mini-master-01:2181 --topic test-topic
```

* Check the result in the consumer terminal window. It should look something like this:
```
Thread-19;Time-432681059397966;Line-<content>
Thread-19;Time-432683833523908;Line-<content>
...
```

## Next Demo steps

The next demo application can be [Kafka-to-HDFS](https://github.com/CiscoCloud/mantl-apps/blob/master/demo-apps/spark-demo-apps/sparkstreaming-kafka-hdfs-simple/DEMO-INSTRUCTIONS-CDH.md) application
