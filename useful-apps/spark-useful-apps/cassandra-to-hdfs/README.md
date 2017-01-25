Cassandra to HDFS
===============

### Description
*Dump table contents to HDFS*

Cassandra to HDFS application reads Cassandra table contents and stores them as a number of files on HDFS.
In case composite partition key discovered each table partition will be stored under separate directory.  

### Requirements
Tested on:

* Java 1.7.0_67
* Maven 3.3.3 (project assembly)
* CDH 5.4.2 & Spark 1.3.0 (job runtime)
* Cassandra 2.2.3

### Assembly
Assembly with Maven.  

```
mvn clean install -Pprod
```

### Configure
Application can be configured from command line providing parameters

* **--host** - is a required property, specify it to point out Cassandra host, example:
```
--host localhost:9042
```
* **--table** - is a required property, specify it to point out Cassadnra table full name including keyspace, example:
```
--table keyspace.table
```
* **--out** - is a required property, specify it to point out directory URI to upload cassandra table contents, example:
```
--out hdfs://localhost/user/examples/files-out"
```
* --bsize - is an optional property, HDFS file block size in Mb, default: governed by dfs.blocksize Hadoop option, example:
```
--bsize 32
```
* --master - is a optional property, specify it to point out spark master URI, by default will be governed by --master option of spark-submit command which would be required in case not providing it application, example: 
```
--master spark://localhost:7077
```
* --name - is a optional property, Application display name, default: Cassandra-to-HDFS , example:
```
--name "Cassandra-to-HDFS"
```
* *--help* - can be used to view usage options from command line.

### Run
Run application via command line, providing mandatory parameters, example:

``` 
spark-submit --class com.cisco.mantl.cassandra.CHDriver --master spark://quickstart.cloudera:7077 jars/cassandra-to-hdfs-1.0.jar -h 192.168.159.130:9042 -t ks2.tab6 -o hdfs://localhost/user/examples1/files-out/cass2
```

### Metrics
Application had been tested on several table data volumes. Runtime described below:

* 1 Gb - 9 min
* 6 Gb - 11 min
* 52 Gb - 210 min


