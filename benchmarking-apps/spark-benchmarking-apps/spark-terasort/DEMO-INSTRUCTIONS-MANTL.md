This demo instructions should be applicable to any DCOS/Mesos managed cluster, i.e. Lambda, Beta, etc.

## Prerequisites

* Login to a cluster and create a directory for jar files. Here is an example for Lambda cluster:

```
ssh centos@128.107.5.0
rm -rf ~/mantl-apps/benchmarking-apps/spark-terasort-1.0-jar-with-dependencies.jar
mkdir -p ~/mantl-apps/benchmarking-apps
cd ~/mantl-apps/benchmarking-apps
wget http://173.39.245.245/files/mantl-apps/benchmarking-apps/spark-terasort-1.0-jar-with-dependencies.jar
ls -altr ~/mantl-apps/benchmarking-apps/spark-terasort-1.0-jar-with-dependencies.jar
```

* Copy jar to HDFS:

```
hdfs dfs -mkdir -p /mantl-apps/benchmarking-apps
hdfs dfs -put -f ~/mantl-apps/benchmarking-apps/spark-terasort-1.0-jar-with-dependencies.jar hdfs://hdfs/mantl-apps/benchmarking-apps
hdfs dfs -ls /mantl-apps/benchmarking-apps
```

## STEP 1 - TeraGen - Generating data

* Remove previous output directory:

```
hdfs dfs -rm -r -f -skipTrash /home/centos/benchmarking-apps/terainput
```

* Run the jar command:

```
/opt/spark/bin/spark-submit --deploy-mode cluster --class com.cisco.mantl.terasort.TeraGen \
 hdfs://hdfs/mantl-apps/benchmarking-apps/spark-terasort-1.0-jar-with-dependencies.jar \
 100G hdfs://10.123.0.153:50071/home/centos/benchmarking-apps/terainput
```

* Check the result:

```
hdfs dfs -du -s -h /home/centos/benchmarking-apps/terainput
```

## STEP 2 - TeraSoft - Sorting data

* Remove previous output directory:

```
hdfs dfs -rm -r -f -skipTrash /home/centos/benchmarking-apps/teraoutput
```

* Run the jar command:

```
/opt/spark/bin/spark-submit --deploy-mode cluster --class com.cisco.mantl.terasort.TeraSort \
 hdfs://hdfs/mantl-apps/benchmarking-apps/spark-terasort-1.0-jar-with-dependencies.jar \
 hdfs://10.123.0.153:50071/home/centos/benchmarking-apps/terainput \
 hdfs://10.123.0.153:50071/home/centos/benchmarking-apps/teraoutput
 
```

* Check the result:

```
hdfs dfs -du -s -h /home/centos/benchmarking-apps/teraoutput
```

## STEP 3 - TeraValidate - Validating data

* Remove previous output directory:

```
hdfs dfs -rm -r -f -skipTrash /home/centos/benchmarking-apps/teravalidate
```

* Run the jar command:

```
/opt/spark/bin/spark-submit --deploy-mode cluster --class com.cisco.mantl.terasort.TeraValidate \
 hdfs://hdfs/mantl-apps/benchmarking-apps/spark-terasort-1.0-jar-with-dependencies.jar \
 hdfs://10.123.0.153:50071/home/centos/benchmarking-apps/teraoutput \
 hdfs://10.123.0.153:50071/home/centos/benchmarking-apps/teravalidate
 
```

* Check the result:

```
hdfs dfs -du -s -h /home/centos/benchmarking-apps/teravalidate
```
