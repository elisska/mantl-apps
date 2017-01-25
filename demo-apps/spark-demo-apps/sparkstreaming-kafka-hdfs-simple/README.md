Spark Streaming Demo Application - Kafka/HDFS
======================================

What it does:
--------------------------------------
* receives messages from Kafka topic
* concatenates Kafka topic name and timestamp to the beginning of each message
* writes modified messages to HDFS directory

Prerequisites:
--------------------------------------
* Spark cluster
* Kafka topic with messages
* HDFS cluster

How to build:
--------------------------------------
* Run `mvn clean package`

How to run:
--------------------------------------
* Build the jar (see above)
* Place the jar file to some shared storage accessible by all Spark workers (hdfs://, or http://, or file://)
* Run the application:

```
spark-submit --class com.cisco.mantl.demo.streaming.KafkaStream \
<path to sparkstreaming-kafka-hdfs-simple-with-dependencies.jar> \
-z <zookeeperQuorum>   \
-g <Kafka consumer group>   \
-t <Kafka topic name>   \
-n <Number of threads> \
-o <Output directory on HDFS>
'
```
For example:

```
spark-submit --class com.cisco.mantl.demo.streaming.KafkaStream \
hdfs:///mantl-apps/demo-apps/sparkstreaming-kafka-hdfs-simple-with-dependencies.jar \
-z 128.107.5.0:2181   \
-g my-group   \
-t test-topic   \
-n 1 \
-o hdfs:///data/kafkastream'
```
