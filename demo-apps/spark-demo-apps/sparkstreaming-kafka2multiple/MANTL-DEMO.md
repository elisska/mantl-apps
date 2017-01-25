## Prerequisites

* Login to a cluster, clone repository, compile needed apps for this demo. Here is an example for LAMBDA cluster:

```
$ cd 
$ git clone https://github.com/CiscoCloud/mantl-apps.git
$ cd mantl-apps/demo-apps/spark-demo-apps/sparkstreaming-kafka2multiple
$ mvn -Dmaven.test.skip=true clean package
$ hdfs dfs -mkdir -p hdfs://hdfs/demo-applications/scripts
$ hdfs dfs -put -f ./target/sparkstreaming-kafka2multiple-0.0.1.jar hdfs://hdfs/demo-applications/scripts
$ cd ../../hadoop/fs-kafka-producer/
$ mvn -Dmaven.test.skip=true clean install -Pprod
```

* Create target Cassandra keyspace and table:

```
# CQLSH_HOST=<Cassandra host IP> cqlsh
> CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };
> CREATE TABLE test.words (word text PRIMARY KEY, count int);
```

* Delete records in output destinations

```
hdfs dfs -rm -r hdfs://hdfs/demo-applications/kafka2hdfs/
echo "truncate test.words;" | CQLSH_HOST=<Cassandra host IP> cqlsh
curl -XDELETE 'http://<ElasticSearch node IP>:<ElasticSearch node port>/sparksql/'
```

## STEP 1 - Produce data to Kafka topic

* Download some sample data
```
$ sudo wget -c "http://labrosa.ee.columbia.edu/millionsong/sites/default/files/AdditionalFiles/unique_terms.txt"
```

* Put this data to HDFS
```
$ hdfs dfs -mkdir hdfs://hdfs/demo-applications/kafka-producer-input/
$ hdfs dfs -put unique_terms.txt hdfs://hdfs/demo-applications/kafka-producer-input/
```

* Use previously compiled [fs-kafka-producer sample application] (https://github.com/CiscoCloud/mantl-apps/tree/master/demo-apps/hadoop-demo-apps/fs-kafka-producer) to produce data from HDFS to Kafka topic:

```
hadoop jar ~/mantl-apps/demo-apps/hadoop-demo-apps/fs-kafka-producer/target/kafka_producer-1.0.jar com.cisco.mantl.kafka.KProdDriver \
--brokers <any Kafka broker IP:port> \
--topic testTopic \
--inputDir hdfs://hdfs/demo-applications/kafka-producer-input \
--zkhost master.mesos:2181
```

## STEP 2 - Run the main application

* Run the main Spark job that reads from Kafka topic and writes to HDFS, Cassandra and ElasticSearch

```
$ spark-submit --deploy-mode cluster --class com.cisco.apps.kafka2multiple hdfs://hdfs/demo-applications/scripts/sparkstreaming-kafka2multiple-0.0.1.jar \
<Kafka brokers> testTopic
hdfs://hdfs/demo-applications/kafka2hdfs
<Elasticsearch node IP:port> sparksql/kafka2es
<Cassandra host IP> test words
```

* Verify output

```
$ hdfs dfs -ls hdfs://hdfs/demo-applications/kafka2hdfs/ 
$ curl -k -XGET 'http://<ElasticSearch node IP:port>/sparksql/_search?size=10&q=*:*&pretty=1'
$ echo "select * from test.words;" | CQLSH_HOST=<Cassandra host IP> cqlsh
```

* Navigate to driver's app sandbox using Mesos web UI (e.g. http://leader.mesos/mesos/...) and open stdout file to watch for statistics. It should look like:

```
Registered executor on dcos-slave-04.novalocal
Starting task driver-20151026100653-0037
sh -c '/opt/spark/bin/spark-submit --name com.cisco.apps.kafka2multiple --class com.cisco.apps.kafka2multiple --master mesos://zk://master.mesos:2181/mesos --driver-cores 1.0 --driver-memory 2048M sparkstreaming-kafka2multiple-0.0.1.jar ....'
Forked command at 25163
Time = 2015.10.26 10:07:43  > HDFS Files = 3 , ES records 2, Cassandra records = 5
Time = 2015.10.26 10:08:24  > HDFS Files = 5 , ES records 5, Cassandra records = 5
Time = 2015.10.26 10:08:59  > HDFS Files = 10 , ES records 10, Cassandra records = 10
Time = 2015.10.26 10:09:34  > HDFS Files = 10 , ES records 10, Cassandra records = 10
Time = 2015.10.26 10:10:08  > HDFS Files = 10 , ES records 10, Cassandra records = 10
Time = 2015.10.26 10:10:43  > HDFS Files = 10 , ES records 10, Cassandra records = 10
Time = 2015.10.26 10:11:18  > HDFS Files = 10 , ES records 10, Cassandra records = 10
Time = 2015.10.26 10:11:53  > HDFS Files = 10 , ES records 10, Cassandra records = 10
Time = 2015.10.26 10:12:29  > HDFS Files = 10 , ES records 10, Cassandra records = 10
Time = 2015.10.26 10:13:04  > HDFS Files = 10 , ES records 10, Cassandra records = 10
Command exited with status 0 (pid: 25163)
```

For demostration purposes application runs for 5 minutse and then exits.
