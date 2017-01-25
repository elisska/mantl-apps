Spark Streaming Demo Application - Kafka/Cassandra
======================================

What it does:
----------------------------------
* receives messages from Kafka topic
* concatenates Kafka topic name and timestamp to the beginning of each message
* writes modified messages to Cassandra table

Prerequisites:
----------------------------------
* Spark cluster
* Kafka topic with messages
* Cassandra database

How to build:
----------------------------------
* Run `mvn clean package`

How to run:
----------------------------------
* Build the jar (see above)
* Place the jar file to some shared storage accessible by all Spark workers (hdfs://, or http://, or file://)
* Create target Cassandra table, use [streaming_sample.cql](https://github.com/CiscoCloud/mantl-apps/blob/master/demo-apps/spark-demo-apps/sparkstreaming-kafka-cassandra-simple/streaming_maple.cql)
* Run the application:

```
spark-submit --class com.cisco.mantl.demo.streaming.KafkaStream \
<path to sparkstreaming-kafka-cassandra-simple-with-dependencies.jar> \
-z <zookeeperQuorum>   \
-g <Kafka consumer group>   \
-t <Kafka topic name>   \
-n <Number of threads> \
-a <Address of cassandra node>   \
-p <Cassandra native port>
```

For example:

```
spark-submit --class com.cisco.mantl.demo.streaming.KafkaStream \
hdfs:///mantl-apps/demo-apps/sparkstreaming-kafka-cassandra-simple-with-dependencies.jar \
-z http://128.107.5.0/:2181   \
-g my-group   \
-t test-topic   \
-n 1 \
-a 10.203.28.112  \
-p 9042
```



