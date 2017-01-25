This demo instructions should be applicable to any Mesos managed cluster, i.e. Lambda, Beta, etc.

## Prerequisites

* Login to a cluster and create a directory for jar files. Here is an example for Lambda cluster:

```
ssh centos@128.107.5.0
rm -rf ~/mantl-apps/demo-apps/sparkstreaming-kafka-hdfs-simple-with-dependencies.jar
mkdir -p ~/mantl-apps/demo-apps
cd ~/mantl-apps/demo-apps
wget http://173.39.245.245/files/demo-apps/sparkstreaming-kafka-hdfs-simple-with-dependencies.jar
ls -altr ~/mantl-apps/demo-apps/sparkstreaming-kafka-hdfs-simple-with-dependencies.jar
```

* Copy jar to HDFS:

```
hdfs dfs -mkdir -p /mantl-apps/demo-apps
hdfs dfs -put -f ~/mantl-apps/demo-apps/sparkstreaming-kafka-hdfs-simple-with-dependencies.jar hdfs://hdfs/mantl-apps/demo-apps
hdfs dfs -ls /mantl-apps/demo-apps
```

* Create Kafka topic *test-topic* (or use the existing one):

```
kafka-topics.sh --create --zookeeper master.mesos:2181 --replication-factor 1 --partition 1 --topic test-topic
```

* Describe available topics:

```
kafka-topics.sh --describe --zookeeper master.mesos:2181
```

## Streaming demo

* Delete HDFS output directory if exists:
   
```
hdfs dfs -rm -r -skipTrash /home/centos/streaming-demo
```

* Run the application:

```
/opt/spark/bin/spark-submit --deploy-mode cluster --class com.cisco.mantl.demo.streaming.KafkaStream hdfs://hdfs/mantl-apps/demo-apps/sparkstreaming-kafka-hdfs-simple-with-dependencies.jar \
	-z master.mesos:2181   \
	-g my-consumer-group   \
	-t test-topic   \
	-n 1 \
	-o hdfs://10.123.0.137:50071/home/centos/streaming-demo/stream
```

* Connect to Kafka topic test-topic to see if messages are coming:

```
kafkacat -C -b lambda-slave-05.novalocal:1025 -t test-topic
```

* Send messages to Kafka topic test-topic:

```
kafkacat -P -b lambda-slave-05.novalocal:1025 -t test-topic
```


* Check the HDFS output:

```
watch -n 'hdfs dfs -ls "/home/centos/streaming-demo/*/part*"'
```


