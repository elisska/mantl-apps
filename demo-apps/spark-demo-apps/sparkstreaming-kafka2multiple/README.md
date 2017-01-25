#Spark Streaming Demo Application - Kafka to HDFS & Cassandra & ElasticSearch
What it does:

- receives messages from Kafka topic
- writes messages to Cassandra table & HDFS output directory & ElasticSearch index in parallel
- every 30 seconds makes sync count to see the speed of data loading (amount of records in HDFS/Cassandra/ES)

Prerequisites:

- Spark cluster
- Kafka topic with messages
- Cassandra database
- HDFS
- ElasticSearch

How to build:

- mvn clean package

How to run:

- Place the jar file on hdfs (e.g. ```hdfs dfs -put <jar file> hdfs://hdfs/```)
- Create target Cassandra keyspace and table (if they don't exist), e.g.:

```
CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };
CREATE TABLE test.words (word text PRIMARY KEY, count int);
```

- Delete records in output destinations

- HDFS: ```hdfs dfs -rm -r hdfs://hdfs/kafka2hdfs/<HDFS output directory>```
- Cassandra: ```echo "truncate <Cassandra keyspace>.<Cassandra table>;" | CQLSH_HOST=<Cassandra host IP> cqlsh```
- ElasticSearch:  ```curl -XDELETE 'http://<ElasticSearch node IP>:<ElasticSearch node port>/<ElasticSearch index>/'```

- Run the application:

```
spark-submit --deploy-mode cluster --class com.cisco.apps.kafka2multiple hdfs://hdfs/sparkstreaming-kafka2multiple-0.0.1.jar \
<Kafka brokers> (e.g. dcos-slave-01:1025) \
<Kafka topic> (e.g. testTopic)
<HDFS output directory link> (e.g. hdfs://hdfs/kafka2hdfs)
<Elasticsearch node and port> (e.g. dcos-slave-01:2182)
<Elasticsearch index/mapping> (e.g. sparksql/kafka2es)
<Cassandra host> (e.g. dcos-slave-02) 
<Cassandra keyspace> (e.g. test)
<Cassandra table> (e.g. words)
```

- Produce some data to Kafka topic using [fs-kafka-producer sample application] (https://github.com/CiscoCloud/mantl-apps/tree/master/demo-apps/hadoop-demo-apps/fs-kafka-producer) from mantl-apps or with simple kafkacat:

```
echo "new"|kafkacat -P -b "<Kafka broker IP:port>" -t testTopic -p 0
```

Navigate to driver's app sandbox using Mesos web UI (e.g. http://leader.mesos/mesos/...) and open stdout file to watch for statistics. It should look like:

```
Registered executor on dcos-slave-04.novalocal
Starting task driver-20151026100653-0037
sh -c '/opt/spark/bin/spark-submit --name com.cisco.apps.kafka2multiple --class com.cisco.apps.kafka2multiple --master mesos://zk://master.mesos:2181/mesos --driver-cores 1.0 --driver-memory 2048M sparkstreaming-kafka2multiple-0.0.1.jar dcos-slave-01:1025 testTopic hdfs://hdfs/kafka2hdfs dcos-slave-01:2182 sparksql/kafka2es dcos-slave-02.novalocal test words'
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

You can also check output data manually in each destination:

1. HDFS: 
```hdfs dfs -ls hdfs://hdfs/<HDFS output directory>/```
2. Elasticsearch: 
```curl -k -XGET 'http://<ElasticSearch node IP:port>/<Index>/_search?size=10&q=*:*&pretty=1'```
3. Cassandra:
```echo "select * from <Cassandra keyspace>.<Cassandra table>;" | CQLSH_HOST=<Cassandra host IP> cqlsh```


For demostration purposes application runs for 5 minutse and then exits.
