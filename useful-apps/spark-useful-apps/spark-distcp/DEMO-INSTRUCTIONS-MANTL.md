## Prerequisites

* swift.conf example:
```
swift.endpoint=https://us-texas-3.cloud.cisco.com:5000/v2.0
swift.username=admin
swift.password=p@ssw0rd
swift.tenant=CIS-DataStore-US-TEXAS-3
```

* Login to a cluster and create a directory for jar files. Here is an example for Lambda cluster:

```
$ git clone https://github.com/CiscoCloud/mantl-apps.git
$ cd mantl-apps/useful-apps/spark-useful-apps/spark-distcp
$ mvn -Dmaven.test.skip=true clean package
$ hdfs dfs -mkdir -p demo-applications/scripts
$ hdfs dfs -put -f ./target/spark-distcp-with-dependencies.jar demo-applications/scripts
```
* **create config file with swift-api configuration(see "swift.conf example")**
* **create an object by link: https://us-texas-3.cloud.cisco.com/horizon/project/containers/sasa/tmp/tmp1.txt**

## STEP 1 - Create dump for object in Swift Storage on HDFS

* Run the job

```
$ spark-submit --deploy-mode cluster --conf spark.executor.memory=24g --conf spark.network.timeout=600 --conf spark.cores.max=210 --conf spark.storage.blockManagerHeartBeatMs=300000 --conf spark.mesos.coarse=true \
--class com.cisco.mantl.SparkDistcp hdfs:///user/$USER/demo-applications/scripts/spark-distcp-with-dependencies.jar \
--swiftConf "/etc/swift.conf" --swiftContainer sasa --swiftUri "/tmp/tmp1.txt" --hdfsUri "demo-applications/tmp.txt" -m fromSwiftToHdfs
```

* Verify output

```
$ hdfs dfs -cat demo-applications/tmp.txt
```

## STEP 2 - Load data from HDFS to Swift Storage

* Run the job

```
$ spark-submit --deploy-mode cluster --conf spark.executor.memory=24g --conf spark.network.timeout=600 --conf spark.cores.max=210 --conf spark.storage.blockManagerHeartBeatMs=300000 --conf spark.mesos.coarse=true \
--class com.cisco.mantl.SparkDistcp hdfs:///user/$USER/demo-applications/scripts/spark-distcp-with-dependencies.jar \
--swiftConf "/etc/swift.conf" --swiftContainer sasa --swiftUri "/tmp/tmp2.txt" --hdfsUri "demo-applications/tmp.txt" -m fromHdfsToSwift
```

* Verify output

In browser go to: https://us-texas-3.cloud.cisco.com/horizon/project/containers/sasa/tmp/

## STEP 3 - Copy data on HDFS

* Run the job

```
$ spark-submit --deploy-mode cluster --conf spark.executor.memory=24g --conf spark.network.timeout=600 --conf spark.cores.max=210 --conf spark.storage.blockManagerHeartBeatMs=300000 --conf spark.mesos.coarse=true \
--class com.cisco.mantl.SparkDistcp hdfs:///user/$USER/demo-applications/scripts/spark-distcp-with-dependencies.jar \
--hdfsSrc "demo-applications/tmp.txt" --hdfsDst "demo-applications/tmp1.txt" -m fromHdfsToHdfs
```

* Verify output

```
$ hdfs dfs -cat demo-applications/tmp1.txt
```