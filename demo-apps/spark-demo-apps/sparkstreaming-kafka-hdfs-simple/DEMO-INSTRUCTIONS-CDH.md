This demo instructions should be applicable to any CDH cluster.

## Prerequisites

Below steps are based on the assumptions:

1) User's home directory on HDFS is `/user/$USER` where `$USER` is the current user logged on to the gateway host.
In case different directory is planned to be used, make sure write permission is available.

2) Kafka topic with messages is available - `test-topic` with `3` partitions

3) Zookeeper quorum is: `chevron-virginia-1-hs3-mini-master-03:2181,chevron-virginia-1-hs3-mini-master-02:2181,chevron-virginia-1-hs3-mini-master-01:2181`

* Login to gateway host and run the below commands:

```
git clone https://github.com/CiscoCloud/mantl-apps
cd mantl-apps/demo-apps/spark-demo-apps/sparkstreaming-kafka-hdfs-simple
mvn clean install
ls -al target/sparkstreaming-kafka-hdfs-simple-with-dependencies.jar
```

* Copy jar to HDFS:
```
hdfs dfs -mkdir -p mantl-apps/demo-apps/
hdfs dfs -put -f target/sparkstreaming-kafka-hdfs-simple-with-dependencies.jar mantl-apps/demo-apps
hdfs dfs -ls mantl-apps/demo-apps/sparkstreaming-kafka-hdfs-simple-with-dependencies.jar
```

* *Optional step:* Create and configure Kafka topic if required:
```
/opt/cloudera/parcels/KAFKA/bin/kafka-topics --create --zookeeper <host name>:<zk port> --replication-factor <number of replicas> --partitions <number of partitions> --topic <topic name>
```

* *Optional step:* Start Kafka producer:
```
/opt/cloudera/parcels/KAFKA/bin/kafka-console-producer --broker-list <broker-host:port> --topic <topic name>
```

## Run Application

* Remove previous output directory:
```
hdfs dfs -rm -r -f -skipTrash data/demo-apps/spark-demo-apps/sparkstreaming-kafka-hdfs-simple
```

* Create output directory:
```
hdfs dfs -mkdir -p data/demo-apps/spark-demo-apps/sparkstreaming-kafka-hdfs-simple
```

* Run spark job:
```
spark-submit --master yarn-cluster --class com.cisco.mantl.demo.streaming.KafkaStream hdfs:///user/$USER/mantl-apps/demo-apps/sparkstreaming-kafka-hdfs-simple-with-dependencies.jar \
-z chevron-virginia-1-hs3-mini-master-03:2181,chevron-virginia-1-hs3-mini-master-02:2181,chevron-virginia-1-hs3-mini-master-01:2181 \
-g my-consumer-group \
-t test-topic \
-n 3 \
-o hdfs:///user/$USER/data/demo-apps/spark-demo-apps/sparkstreaming-kafka-hdfs-simple/demo
```

* *Optional step:* Supply data into Kafka producer

* Check the result in the output directory:

```
hdfs dfs -ls -h "/user/$USER/data/demo-apps/spark-demo-apps/sparkstreaming-kafka-hdfs-simple/*/*"
```

```
watch -n 10 'hdfs dfs -ls -h "/user/$USER/data/demo-apps/spark-demo-apps/sparkstreaming-kafka-hdfs-simple/demo-*/part-*" | wc -l '
```

* Stop application by killing it in YARN:

```
yarn application --kill <application-id>
```
