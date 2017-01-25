This demo instructions should be applicable to any CDH cluster.

## Prerequisites

* Make sure your user has home directory on HDFS (or any other directory with write permissions).

The below steps are based on the assumptions:

1) user's home directory on HDFS is `/user/$USER` where `$USER` is the current user logged on to the gateway host.

2) Data is already present in Cassandra table

3) Output directory: `hdfs:///user/$USER/data/cassandra-output`
 
 * Login to a gateway host and run the below commands:
 
```
$ git clone https://github.com/CiscoCloud/mantl-apps.git
$ cd mantl-apps/useful-apps/spark-useful-apps/cassandra-to-hdfs
$ mvn clean install -Pprod
ls -al target/cassandra-to-hdfs-1.0.jar
```
 
 * Copy jar to HDFS:
 
```
hdfs dfs -mkdir -p mantl-apps/useful-apps
hdfs dfs -put -f target/cassandra-to-hdfs-1.0.jar mantl-apps/useful-apps
hdfs dfs -ls mantl-apps/useful-apps/cassandra-to-hdfs-1.0.jar
```


 
## Run application

 * Output directory should not exist, remove target if applicable
 
 * Provide required parameters and run application. For the full list of parameters refer to [README](https://github.com/CiscoCloud/mantl-apps/blob/master/useful-apps/spark-useful-apps/cassandra-to-hdfs/README.md).
``` 
spark-submit --class com.cisco.mantl.cassandra.CHDriver --master yarn-cluster hdfs:///user/$USER/mantl-apps/useful-apps/cassandra-to-hdfs-1.0.jar -h <cass host>:<cass port> -t <keyspace>.<table> -o hdfs:///user/$USER/data/cassandra-output
```
 * Check `hdfs:///user/$USER/data/cassandra-output` for the output. In case target table had a composite partition key output should be divided into subfolders like part1-part2-etc. 
 
 
 