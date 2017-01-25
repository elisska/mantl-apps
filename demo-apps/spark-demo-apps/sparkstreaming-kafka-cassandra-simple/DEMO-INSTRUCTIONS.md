#################################################
#				DEMO TO SHOW					#
#################################################

TO BE DONE!!!!!!

Environment to run the application:

* DCOS cluster with Spark, HDFS and Kafka installed:
http://173.39.243.105

* Built streaming-jar-with-dependencied.jar stored on HDFS:
hdfs dfs -ls hdfs:///jars/streaming-jar-with-dependencied.jar

* Kafka brockers and Kafka topics

** Checking brockers:
dcos kafka status

** Checking topics:
*** Log in to dcos-slave-09 where Kafka framework is installed
    ssh centos@173.39.243.106 -i gluon-root.pem
*** CD to Kafka home:
    cd /var/lib/mesos/slave/slaves/20150807-143351-1945315245-5050-14143-S0/frameworks/20150807-143351-1945315245-5050-14143-0000/executors/kafka.0c8ccdd9-4d68-11e5-8962-56847afe9799/runs/latest/kafka_2.10-0.8.2.1/bin
*** Export JAVA_HOME:
    export JAVA_HOME=`pwd`/../../jre
*** Check existing topics:
    ./kafka-topics.sh --describe --zookeeper 173.39.243.105:2181 --topic test-oklev
*** Start producing Kafka messages:
    ./kafka-console-producer.sh --broker-list dcos-slave-01.novalocal:1025,dcos-slave-05.novalocal:1025 --topic test-oklev
*** To consume Kafka messages (just to see that messages can be consumed from topic):
    ./kafka-console-consumer.sh --zookeeper 173.39.243.105:2181 --topic test-oklev --from-beginning

* Run the application (from DCOS master node):
** Delete HDFS output directory if exists:
   hdfs dfs -rm -r -skipTrash /data/kafka*
** Run the application:
	dcos spark run --submit-args='--class com.cisco.spark.streaming.KafkaStream hdfs:///jars/streaming-jar-with-dependencies.jar \
	-z 173.39.243.105:2181   \
	-g my-consumer-group   \
	-t test-oklev   \
	-n 1 \
	-o /data/kafka \
	'
** Sent messages to Kafka topic test-oklev (from dcos-slave-09)
** Check the HDFS output:
hdfs dfs -ls /data/kafka*/part*
