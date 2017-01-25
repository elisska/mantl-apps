## Prerequisites

* Login to a cluster, clone repository, compile app for this demo. Here is an example for LAMBDA cluster:

```
$ git clone https://github.com/CiscoCloud/mantl-apps.git
$ cd mantl-apps/demo-apps/spark-demo-apps/sparkstreaming-kafkamonitor
$ mvn -Dmaven.test.skip=true clean package
$ hdfs dfs -mkdir -p hdfs://hdfs/demo-applications/scripts
$ hdfs dfs -put -f ./target/sparkstreaming-kafkamonitor-0.0.1.jar hdfs://hdfs/demo-applications/scripts
```

## Run the main application

* Run the job that will read from Shipped Central Kafka topic logstash and write alerts to Shipped Central Elastic Search index for those utilization metrics that go beyond 5% (for CPU) and 60% (for RAM):
```
$ /opt/spark/bin/spark-submit --deploy-mode cluster --class com.cisco.apps.kafkamonitor \
hdfs://hdfs/demo-applications/scripts/sparkstreaming-kafkamonitor-0.0.1.jar \
128.107.15.5:2182,128.107.15.50:2182,128.107.15.51:2182,128.107.15.52:2182,128.107.15.53:2182,128.107.15.54:2182,128.107.15.55:2182 \
logstash \
5 60 \
128.107.15.5:3889,128.107.15.50:1025,128.107.15.51:1025,128.107.15.52:5052,128.107.15.53:3889,128.107.15.54:1025,128.107.15.55:3889 sparkmon/alerts
```

You can always find updated lists of Kafka brokers and ElasticSearch nodes on [Shipped Central page](https://github.com/CiscoCloud/ShippedAnalytics)

* Verify output

```
$ curl -k -XGET 'http://128.107.15.5:3889/sparkmon/_search?size=50&q=*:*&pretty=1'
...
  "hits" : {
    "total" : 67,
    "max_score" : 1.0,
    "hits" : [ {
      "_index" : "sparkmon",
      "_type" : "alerts",
      "_id" : "AVDTAQtO9c-JyarGCFSI",
      "_score" : 1.0,
      "_source":{"host":"shipped-tx3-worker-003.novalocal","date":"2015-11-04T14:56:35.607Z","plugin":"cpu","metric":87.01228678668558}
    }, {
      "_index" : "sparkmon",
      "_type" : "alerts",
      "_id" : "AVDTAoT85qRsou5Cwsqq",
      "_score" : 1.0,
      "_source":{"host":"shipped-tx3-control-01.novalocal","date":"2015-11-04T14:56:50.624Z","plugin":"cpu","metric":75.0469451735353}
    }, {

```


For demostration purposes application runs for 5 minutes and then exits.
