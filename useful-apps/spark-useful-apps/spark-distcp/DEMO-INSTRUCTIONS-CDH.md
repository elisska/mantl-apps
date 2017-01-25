## Prerequisites

* swift.conf example:
```
swift.endpoint=https://us-texas-3.cloud.cisco.com:5000/v2.0
swift.username=admin
swift.password=p@ssw0rd
swift.tenant=CIS-DataStore-US-TEXAS-3
```

* Login to a cluster and create a directory for jar files. Here is an example for CDH cluster:

```
git clone https://github.com/CiscoCloud/mantl-apps.git
cd mantl-apps/useful-apps/spark-useful-apps/spark-distcp
mvn -Dmaven.test.skip=true clean package
hdfs dfs -mkdir -p mantl-apps/useful-apps
hdfs dfs -put -f target/spark-distcp-with-dependencies.jar mantl-apps/useful-apps
```
* **create config file with swift-api configuration(see "swift.conf example")**
* **create an object by link: https://us-texas-3.cloud.cisco.com/horizon/project/containers/sasa/tmp/tmp1.txt**

## STEP 1 - Dump data from Swift to HDFS

* Run the job

```
$ spark-submit --class com.cisco.mantl.SparkDistcp hdfs:///user/$USER/demo-applications/scripts/spark-distcp-with-dependencies.jar \
--swiftConf "/etc/swift.conf" --swiftContainer sasa --swiftUri "/tmp/tmp1.txt" --hdfsUri "demo-applications/tmp.txt" -m fromSwiftToHdfs
```

* Verify output

```
$ hdfs dfs -du -s -h /data/sasa-logs/20150224
```

## STEP 2 - Load data from HDFS to Swift Storage

* Run the job

```
$ spark-submit --class com.cisco.mantl.SparkDistcp hdfs:///user/$USER/demo-applications/scripts/spark-distcp-with-dependencies.jar \
--swiftConf "/etc/swift.conf" --swiftContainer sasa --swiftUri "/tmp/tmp2.txt" --hdfsUri "demo-applications/tmp.txt" -m fromHdfsToSwift
```

* Verify output

In browser go to: https://us-texas-3.cloud.cisco.com/horizon/project/containers/sasa/tmp/

## STEP 3 - Copy data from HDFS to HDFS

* Run the job

```
spark-submit --master yarn-cluster --class com.cisco.mantl.SparkDistcp hdfs:///user/$USER/mantl-apps/useful-apps/spark-distcp-with-dependencies.jar \
--hdfsSrc "demo-applications/tmp.txt" --hdfsDst "demo-applications/tmp1.txt" -m fromHdfsToHdfs
```

* Verify output

```
$ hdfs dfs -cat demo-applications/tmp1.txt
```
