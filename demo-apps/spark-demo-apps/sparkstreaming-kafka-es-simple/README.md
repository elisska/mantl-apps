Spark Integration
=================

### Description
*Integrate Kafka messaging system and Elasticsearch engine using Spark job.*

Spark Integration is a Spark streaming job application which serves as a Kafka consumer sending JSON data into Elasticsearch index. **NOTE:** Data to be consumed should be valid JSON document. Otherwise JsonParseException will be introduced into job log.

### Requirements
Tested on:

* Maven 3.3.3 (project assembly)
* CDH 5.4.2 & Spark 1.3.0 (job runtime)
* Elasticsearch 1.7.2
* Apache Kafka 0.8.2.2


### Assembly
Assembly with Maven.  

```
mvn clean install
```
Use **spark_integration-1.0.jar** with included dependencies

### Configure
Application can be configured from command line providing parameters

* **--brokers** - is a required property, specify it as comma separated list to point out brokers Kafka stream to be created on, example:
```
--brokers broker1_host:port,broker2_host:port
```
* **--topics** - is a required property, specify it as comma separated list to point out topics to consume from, example:
```
--topics topic1,topic2
```
* --host - is a optional property, specify it to point out Elasticsearch host, default: localhost:9200, example:
```
--host localhost:9200
```
* **--index** - is a required property, specify it to point out Elasticsearch index, example:
```
--index someindex
```
* **--type** - is a required property, specify it to point out Elasticsearch type, example:
```
--type sometype
```
* --master - is a optional property, specify it to point out spark master URI, by default will be governed by --master option of spark-submit command which would be required in case not providing it to File Aggregator application, example: 
```
--master spark://quickstart.cloudera:7077
```
* --name - is a optional property, Application display name, default: File Aggregator , example:
```
--name "File Agg"
```
* --batchint - is an optional property, defines duration in seconds between consuming topic messages, default: 1 , example:
```
--batchint 2
```
* *--help* - can be used to view usage options from command line.

### Run
Run application by submitting it to Spark via command line, providing mandatory parameters, prior make sure Elasticsearch and Kafka servers are up and running example:

``` 
spark-submit --class "com.cisco.mantl.integration.ItgDriver" --master local jars/spark_integration-1.0.jar --brokers localhost:9092 --topics test --index someindex --type sometype --batchint 2
```
Feed JSON input into Kafka producer, example:
``` 
{"name": "Silvester's Italian restaurant", "description": "Great food, great atmosphere!", "address": { "street": "46 W 46th street", "city": "New York", "state": "NY", "zip": "10036" }, "location": [40.75, -73.97], "tags": ["italian", "spagetti", "pasta"], "rating": "3.5" }
```
Check Elasticsearch relevant index, example using REST client:
``` 
http://localhost:9200/someindex/sometype/_search?q=state:NY
```
Comprehensive result should be available on this point.

### Known Issues
There is a slight possibility **consumeAndPutJsonDataSuccessfully** test will fail due to **org.elasticsearch.indices.IndexMissingException**. This usually happens if embedded Spark engine wasn't able to consume data from Kafka to ES index till the moment ES client will start querying mentioned index.
This is simply handled by introducing a delay. For the moment there is not any sync mechanism to handle this situation graciously as all the actor components are embedded and inherently communicating asynchronously. Run the test:

``` 
mvn test
```