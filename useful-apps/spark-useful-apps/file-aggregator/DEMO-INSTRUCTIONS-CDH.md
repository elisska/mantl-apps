This demo instructions should be applicable to any CDH cluster.

## Prerequisites

* Make sure your user has home directory on HDFS (or any other directory with write permissions).

The below steps are based on the assumptions:

1) user's home directory on HDFS is `/user/$USER` where `$USER` is the current user logged on to the gateway host.

2) input dataset is in `hdfs:///data/sasa-logs` directory.

* Login to a gateway host and run the below commands:

```
git clone https://github.com/CiscoCloud/mantl-apps
cd mantl-apps/useful-apps/spark-useful-apps/file-aggregator
mvn clean package
ls -al target/file-aggregator-with-dependencies.jar
```

* Copy jar to HDFS:

```
hdfs dfs -mkdir -p mantl-apps/useful-apps
hdfs dfs -put -f target/file-aggregator-with-dependencies.jar mantl-apps/useful-apps
hdfs dfs -ls mantl-apps/useful-apps/file-aggregator-with-dependencies.jar
```

## MODE 1 - File Aggregation with default parameters

Default parameters are:

Max output file size: 128 MB

Output file block size: dfs.blocksize

Spark application name: File Aggregator

Include input directories recursively: true

* Remove previous output directory:

```
hdfs dfs -rm -r -f -skipTrash /data/sasa-logs-without-recursion
```

* Run the jar command:

```
spark-submit --master yarn-cluster --class com.cisco.mantl.aggregator.AggDriver \
 hdfs:///user/$USER/mantl-apps/useful-apps/file-aggregator-with-dependencies.jar \
 -i hdfs:///data/sasa-logs \
 -o hdfs:///data/sasa-logs-without-recursion
```

* Check the result (comparing size of input and output directories):

```
hdfs dfs -du -s -h hdfs:///data/sasa-logs
hdfs dfs -du -s -h hdfs:///data/sasa-logs-without-recursion
```

## MODE 2 - File Aggregation with custom parameters

For example:

Max output file size: 1024MB

Output file block size: 256MB

* Remove previous output directory:

```
hdfs dfs -rm -r -f -skipTrash hdfs:///data/sasa-logs-without-recursion-custom
```

* Run the jar command:

```
spark-submit --master yarn-cluster --class com.cisco.mantl.aggregator.AggDriver \
 hdfs:///user/$USER/mantl-apps/useful-apps/file-aggregator-with-dependencies.jar \
 -i hdfs:///data/sasa-logs \
 -o hdfs:///data/sasa-logs-without-recursion-custom \
 -f 1024 -b 256 
```

* Check the result (comparing size of input and output directories):

```
hdfs dfs -du -s -h hdfs:///data/sasa-logs
hdfs dfs -du -s -h hdfs:///data/sasa-logs-without-recursion-custom
```

## Next Demo steps

The next demo application could be [Kafka Producer](https://github.com/CiscoCloud/mantl-apps/blob/master/demo-apps/hadoop-demo-apps/fs-kafka-producer/DEMO-INSTRUCTIONS-CDH.md) application
