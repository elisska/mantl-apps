Kafka Producer
===============

### Description
*Read files contents and forward to Kafka*

Kafka Producer application reads files from specified directory and forwards contents line by line into Kafka broker.
Each file is being read in a separate thread. Thread pool size depends on a target Kafka topic partition number property.  

### Requirements
Tested on:

* Maven 3.3.3 (project assembly)
* CDH 5.4.2 (runtime)


### Assembly
Assembly with Maven.  

```
mvn clean install -Pprod
```

### Configure
Application can be configured from command line providing parameters

* **--brokers** - is a required property, specify it as comma separated list to point out brokers, example:
```
--brokers broker1_host:port,broker2_host:port
```
* **--topics** - is a required property, specify it as comma separated list to point out topics messages will be produced to, example:
```
--topics topic1,topic2
```
* **--inputDir** - is a required property, specify it to point out root directory files should be read from, example:
```
--inputDir hdfs://localhost:8020/user/examples/
```
* --zkhost - is an optional property, defines zookeeper host Kafka broker runs on, default: localhost:2181, example:
```
--zkhost localhost:2181
```
* *--help* - can be used to view usage options from command line.

### Run
Run application via command line, providing mandatory parameters, example:

``` 
hadoop jar jars/kafka_producer-1.0.jar com.cisco.mantl.kafka.KProdDriver --brokers localhost:9092 --topic test --inputDir hdfs://localhost:8020/user/examples1/files --zkhost localhost:2181
```