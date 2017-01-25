#Spark Streaming Demo Application - Kafka Monitor
What it does:

- receives messages from Kafka topic about CPU and RAM utilization of different hosts
- If average CPU or RAM utilization is beyond given threshold - fires an alarm to Elasticsearch index

Application utilizes Spark Streaming and Spark SQL

Prerequisites:

- Spark cluster
- Kafka topic with messages about CPU and RAM utilization (use Shipped Analytics cluster for demo purposes)
- ElasticSearch

How to build:

- mvn clean package

How to run:

- Place the jar file on hdfs
- Run the application:

```
spark-submit --deploy-mode cluster --class com.cisco.apps.kafkamonitor hdfs://hdfs/sparkstreaming-kafkamonitor-0.0.1.jar \
<Kafka brokers> (e.g. dcos-slave-02:1025) \
<Kafka topic with metrics> (e.g. logstash) \
<CPU utilization threshold in %> (e.g. 80) \ 
<RAM utilization threshold in %> (e.g. 80) \
<Elasticsearch IP:port> (e.g. dcos-slave-02:3889) \
<Elasticsearch index> (e.g. sparkmon/alerts)
```

Shipped Central Kafka Brokers:
```
128.107.15.5:2182,128.107.15.50:2182,128.107.15.51:2182,128.107.15.52:2182,128.107.15.53:2182,128.107.15.54:2182,128.107.15.55:2182
```

Shipped Central Elasticsearch nodes:
```
128.107.15.55:5052,128.107.15.52:1025,128.107.15.54:5052,128.107.15.5:5052,128.107.15.53:3889,128.107.15.50:8082,128.107.15.51:5052
```

For demostration purposes application runs for 5 minutes and then exits.

Check alarms in Elasticsearch index, e.g.:
```curl -k -XGET 'http://<Elasticsearch node IP:port>/sparkmon/_search?size=50&q=*:*&pretty=1'```
