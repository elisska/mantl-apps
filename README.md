# Applications for MANTL platform

This repository contains some Spark- and Hadoop-based applications that can be run on MANTL as well as CDH platform. 

Below is the full list of applications.

## Benchmarking applications

| Application Name | Short Description    | Can be run on MANTL? | Can be run on plain CDH? | 
| ---------------- | -------------------- | ------------------------ | ----------------- |
| [TeraSort](benchmarking-apps/spark-benchmarking-apps/spark-terasort) | Spark-based TeraSort benchmark | YES, [MANTL-DEMO](benchmarking-apps/spark-benchmarking-apps/spark-terasort/DEMO-INSTRUCTIONS-MANTL.md) | YES, [CDH-DEMO](benchmarking-apps/spark-benchmarking-apps/spark-terasort/DEMO-INSTRUCTIONS-CDH.md) |
| [TestDFSIO](benchmarking-apps/spark-benchmarking-apps/spark-test-dfsio) | Spark-based TestDFSIO benchmark | YES, [MANTL-DEMO](benchmarking-apps/spark-benchmarking-apps/spark-test-dfsio/DEMO-INSTRUCTIONS-MANTL.md) | YES, [CDH-DEMO](benchmarking-apps/spark-benchmarking-apps/spark-test-dfsio/DEMO-INSTRUCTIONS-CDH.md) |


## Useful applications

| Application Name | Short Description    | Can be run on MANTL? | Can be run on plain CDH? | 
| ---------------- | -------------------- | ------------------------ | ----------------- |
| [Spark-based DistCP](useful-apps/spark-useful-apps/spark-distcp) | Application to copy data from HDFS to HDFS, from Swift to HDFS and vice versa  | YES, [MANTL-DEMO](useful-apps/spark-useful-apps/spark-distcp/DEMO-INSTRUCTIONS-MANTL.md) | YES, [CDH-DEMO](useful-apps/spark-useful-apps/spark-distcp/DEMO-INSTRUCTIONS-CDH.md) |
| [File Aggregator](useful-apps/spark-useful-apps/file-aggregator) | Application to merge small files on HDFS | YES, [MANTL-DEMO](useful-apps/spark-useful-apps/file-aggregator/DEMO-INSTRUCTIONS-MANTL.md) | YES, [CDH-DEMO](useful-apps/spark-useful-apps/file-aggregator/DEMO-INSTRUCTIONS-CDH.md) | 
| [Cassandra-to-HDFS](useful-apps/spark-useful-apps/cassandra-to-hdfs) | Application to download Cassandra table on HDFS | YES, [MANTL-DEMO](useful-apps/spark-useful-apps/cassandra-to-hdfs/DEMO-INSTRUCTIONS-MANTL.md) | YES, [CDH-DEMO](useful-apps/spark-useful-apps/cassandra-to-hdfs/DEMO-INSTRUCTIONS-CDH.md) | 


## Demo applications

| Application Name | Short Description    | Can be run on MANTL? | Can be run on plain CDH? | 
| ---------------- | -------------------- | ------------------------ | ----------------- |
| [Kafka Producer](demo-apps/hadoop-demo-apps/fs-kafka-producer) | Reads directory and send messages to Kafka topic  | YES, [MANTL-DEMO](demo-apps/hadoop-demo-apps/fs-kafka-producer/DEMO-INSTRUCTIONS-MANTL.md) | YES, [CDH-DEMO](demo-apps/hadoop-demo-apps/fs-kafka-producer/DEMO-INSTRUCTIONS-CDH.md) | 
| [Kafka-to-HDFS](demo-apps/spark-demo-apps/sparkstreaming-kafka-hdfs-simple) | Reads messages from Kafka topic and writes them to HDFS  | YES, [MANTL-DEMO](demo-apps/spark-demo-apps/sparkstreaming-kafka-hdfs-simple/DEMO-INSTRUCTIONS-MANTL.md) | YES, [CDH-DEMO](demo-apps/spark-demo-apps/sparkstreaming-kafka-hdfs-simple/DEMO-INSTRUCTIONS-CDH.md) |
| [Kafka-to-Cassandra](demo-apps/spark-demo-apps/sparkstreaming-kafka-cassandra-simple) | Reads messages from Kafka topic and writes them to Cassandra  | YES | NO, requires Cassandra installation |
| [Kafka-to-ES](demo-apps/spark-demo-apps/sparkstreaming-kafka-es-simple) | Reads messages from Kafka topic and writes them to Emastic Search  | YES | NO, requires ES installation |
| [Kafka-to-Multiple](demo-apps/spark-demo-apps/sparkstreaming-kafka2multiple) | Reads messages from Kafka topic and writes them to HDFS, Cassandra and Elastic Search, runs a parallel thread that counts amount of saved records in each destination  | YES, [MANTL-DEMO](demo-apps/spark-demo-apps/sparkstreaming-kafka2multiple/MANTL-DEMO.md) | NO, requires Cassandra and ES installation |
| [Kafka Monitor](demo-apps/spark-demo-apps/sparkstreaming-kafkamonitor) | Reads messages from Kafka topic on Shipped Central cluster (containing information about CPU/RAM utilization of different hosts) and writes alerts to Elastic Search if any of these metrics are beyond given thresholds  | YES, [MANTL-DEMO](demo-apps/spark-demo-apps/sparkstreaming-kafkamonitor/MANTL-DEMO.md) | NO, requires ES installation and is specific to ShippedCentral |
