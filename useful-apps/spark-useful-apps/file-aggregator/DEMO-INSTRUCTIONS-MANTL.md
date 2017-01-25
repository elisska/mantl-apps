This demo instructions should be applicable to any MANTL-managed cluster.

## Prerequisites

* Make sure your user has home directory on HDFS (or any other directory with write permissions).

The below steps are based on the assumption that user's home directory on HDFS is `/user/$USER` where `$USER` is the current user logged on to the gateway host.

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
hdfs dfs -put -f file-aggregator-with-dependencies.jar mantl-apps/useful-apps
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
hdfs dfs -rm -r -f -skipTrash data/file-aggregator-output
```

* Run the jar command:

```
spark-submit --deploy-mode cluster --class com.cisco.mantl.aggregator.AggDriver \
 hdfs:///user/$USER/mantl-apps/useful-apps/file-aggregator-with-dependencies.jar \
 -i hdfs:///data/sasa-logs/20150224/www.cisco.com \
 -o hdfs:///user/$USER/data/file-aggregator-output
```

* Check the result (comparing size of input and output directories):

```
hdfs dfs -du -s -h hdfs:///user/$USER/data/file-aggregator-output
hdfs dfs -du -s -h hdfs:///data/sasa-logs/20150224/www.cisco.com
```

## MODE 2 - File Aggregation with custom parameters

For example:

Max output file size: 1024MB

Output file block size: 256MB

* Remove previous output directory:

```
hdfs dfs -rm -r -f -skipTrash data/file-aggregator-output-custom
```

* Run the jar command:

```
spark-submit --deploy-mode cluster --class com.cisco.mantl.aggregator.AggDriver \
 hdfs:///user/$USER/mantl-apps/useful-apps/file-aggregator-with-dependencies.jar \
 -i hdfs:///data/sasa-logs/20150224/www.cisco.com \
 -o hdfs:///user/$USER/data/file-aggregator-output-custom \
 -f 1024 -b 256 
```

* Check the result (comparing size of input and output directories):

```
hdfs dfs -du -s -h hdfs:///user/$USER/data/file-aggregator-output-custom
hdfs dfs -du -s -h hdfs:///data/sasa-logs/20150224/www.cisco.com
```


