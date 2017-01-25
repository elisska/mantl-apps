HAT-data-generator
===============

### Description
*Generate pseudo human activity data and save it to HDFS*

HAT-data-generator application generates pseudo data of human activity in order to be further analysed. As a basis data model taken from http://www.cis.fordham.edu/wisdm/dataset.php
Data is being saved to HDFS as a set of directories, 1 per user. Data format follows this format:
```
[user],[activity],[timestamp],[x-acceleration],[y-accel],[z-accel]
```

Representative data example:
```
1,Downstairs,96688372472,-5.470834779769724,-1.5037017960233374,-7.311290105459875
```
Fields:
-user - nominal 1.. - will be incrementer for each consequent batch;
-activity - nominal { Walking, Jogging, Sitting, Standing, Upstairs,	Downstairs }
-timestamp - numeric - generated long
-x-acceleration	numeric, floating-point values between -20 .. 20
-y-acceleration	numeric, floating-point values between -20 .. 20
-z-acceleration	numeric, floating-point values between -20 .. 20


### Requirements
Tested on:

* Java 1.7.0_67
* Maven 3.3.3 (project assembly)
* CDH 5.4.2 & Spark 1.3.0 (job runtime)


### Assembly
Assembly with Maven.  

```
mvn clean install -Pprod
```

### Configure
Application can be configured from command line providing parameters

* **--out** - required property, directory URI to upload generator output, example:"
```
--out hdfs://localhost/user/examples/files-out"
```
* **--amount** - required property, amount of data in megabytes to be generated, example:
```
--amount 100
```
* --frequency - optional property, batch frequency in seconds, default is set to 3 seconds, example:
```
--frequency 5
```
* --batchsize - optional property, amount of data in megabytes to be generated per batch, affects number of output directories, default: 1, example:
```
--batchsize 10
```
* --blocksize - is an optional property, HDFS file block size in Mb, default: governed by dfs.blocksize Hadoop option, example:
```
--blocksize 32
```
* --master - is a optional property, specify it to point out spark master URI, by default will be governed by --master option of spark-submit command which would be required in case not providing it application, example: 
```
--master spark://localhost:7077
```
* --name - is a optional property, Application display name, default: Cassandra-to-HDFS , example:
```
--name "Cassandra-to-HDFS"
```
* --print - optional property, enable/disable batch contents to be printed to console output, default: false, example:
```
--print true
```
* *--help* - can be used to view usage options from command line.

### Run
Run application via command line, providing mandatory parameters, example:

``` 
spark-submit --master spark://quickstart.cloudera:7077 --class com.cisco.mantl.hat.HATGenDriver jars/hat-data-generator-1.0.jar --out hdfs://localhost/user/examples1/files-out/out --amount 3 --frequency 2 --batchsize 1 --print true
```