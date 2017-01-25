This demo instructions should be applicable to any CDH cluster.

## Prerequisites

* Make sure your user have home directory on HDFS (or any other directory with write permissions).

The below steps are based on the assumption that user's home directory on HDFS is `/user/$USER` where `$USER` is the current user logged on to the gateway host.

* Login to a gateway host and run the below commands:

```
git clone https://github.com/CiscoCloud/mantl-apps
cd mantl-apps/benchmarking-apps/spark-benchmarking-apps/spark-terasort
mvn clean install
ls -al target/spark-terasort-1.0-jar-with-dependencies.jar
```

* Copy jar to HDFS:

```
hdfs dfs -mkdir -p mantl-apps/benchmarking-apps
hdfs dfs -put -f target/spark-terasort-1.0-jar-with-dependencies.jar mantl-apps/benchmarking-apps
hdfs dfs -ls mantl-apps/benchmarking-apps/spark-terasort-1.0-jar-with-dependencies.jar
```

## STEP 1 - TeraGen - Generating data

* Remove previous output directory:

```
hdfs dfs -rm -r -f -skipTrash data/terainput
```

* Run the jar command:

```
spark-submit --master yarn-cluster --conf spark.dynamicAllocation.enabled=false \
--num-executors 20 --class com.cisco.mantl.terasort.TeraGen hdfs:///user/$USER/mantl-apps/benchmarking-apps/spark-terasort-1.0-jar-with-dependencies.jar \
100G data/terainput
```

* Check the result:

```
hdfs dfs -du -s -h data/terainput
```

## STEP 2 - TeraSoft - Sorting data

* Remove previous output directory:

```
hdfs dfs -rm -r -f -skipTrash data/teraoutput
```

* Run the jar command:

```
spark-submit --master yarn-cluster --class com.cisco.mantl.terasort.TeraSort hdfs:///user/$USER/mantl-apps/benchmarking-apps/spark-terasort-1.0-jar-with-dependencies.jar data/terainput data/teraoutput
```

* Check the result:

```
hdfs dfs -du -s -h data/teraoutput
```

## STEP 3 - TeraValidate - Validating data

* Remove previous output directory:

```
hdfs dfs -rm -r -f -skipTrash data/teravalidate
```

* Run the jar command:

```
spark-submit --master yarn-cluster --class com.cisco.mantl.terasort.TeraValidate hdfs:///user/$USER/mantl-apps/benchmarking-apps/spark-terasort-1.0-jar-with-dependencies.jar data/teraoutput \
 data/teravalidate
```

* Check the result:

```
hdfs dfs -du -s -h data/teravalidate
```
