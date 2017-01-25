This demo instructions should be applicable to any CDH cluster.

## Prerequisites

* Make sure your user have home directory on HDFS (or any other directory with write permissions).

The below steps are based on the assumption that user's home directory on HDFS is `/user/$USER` where `$USER` is the current user logged on to the gateway host.

* Login to a gateway host and run the below commands:

```
git clone https://github.com/CiscoCloud/mantl-apps
cd mantl-apps/benchmarking-apps/spark-benchmarking-apps/spark-test-dfsio
mvn clean package
ls -al target/spark-test-dfsio-with-dependencies.jar
```

* Copy jar to HDFS:

```
hdfs dfs -mkdir -p mantl-apps/benchmarking-apps
hdfs dfs -put -f target/spark-test-dfsio-with-dependencies.jar mantl-apps/benchmarking-apps
hdfs dfs -ls mantl-apps/benchmarking-apps/spark-test-dfsio-with-dependencies.jar
```

## STEP 1 - Testing WRITE performance

* Remove previous output directory:

```
hdfs dfs -rm -r -f -skipTrash data/testdfsio-write
```

* Run the jar command:

```
spark-submit --master yarn-cluster --class com.cisco.dfsio.test.Runner hdfs:///user/$USER/mantl-apps/benchmarking-apps/spark-test-dfsio-with-dependencies.jar --file data/testdfsio-write --nFiles 10 --fSize 200000000 -m write --log data/testdfsio-write/testHdfsIO-WRITE.log
```

* Check the result:

```
hdfs dfs -cat data/testdfsio-write/testHdfsIO-WRITE.log
```

## STEP 2 - Testing READ performance

* Remove previous output directory:

```
hdfs dfs -rm -r -f -skipTrash data/testdfsio-write
```

* Run the jar command:

```
spark-submit --master yarn-cluster --class com.cisco.dfsio.test.Runner hdfs:///user/$USER/mantl-apps/benchmarking-apps/spark-test-dfsio-with-dependencies.jar --file data/testdfsio-write --nFiles 10 --fSize 200000000 -m read --log data/testdfsio-write/testHdfsIO-READ.log
```

* Check the result:

```
hdfs dfs -cat data/testdfsio-write/testHdfsIO-READ.log
```

