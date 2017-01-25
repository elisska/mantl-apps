# Development Environment

Any developer on CIS project is encouraged to use this development environment in a daily development!!!

Here are useful links:

* [How to connect to VPN](#how-to-connect-to-vpn)
* [Development Virtual Machine](#development-virtual-machine)
* [CDH cluster](#cdh-cluster)
* [Cassandra database](#cassandra-database)

## How to connect to VPN

Befure using the cluster you shoudl connect to the cluster's VPN.

Here are the details about the VPN node in `CIS-DataStore-US-TEXAS-3` tenant:

| HOSTNAME      | OS	   | IP ADDRESS	 | RAM	| VCPU	| FLAVOUR	| KEY |
| ------------- |--------- | ----------- | ---- | ----- | ------------- | --- |
| vpn   | CentOS-7 | 10.100.0.40, 128.107.2.166 | 2	| 1	| Micro-Small	| cdh | 

Use *Cisco AnyConnect Secure Mobile Client* or *Openconnect VPN client* to connect to VPN.

Vpn address to access: `128.107.2.166`

Vpn credentials are: `vpnuser/7e4kU6Qh`


## Development Virtual Machine

Here are the details about the development VM in `CIS-DataStore-US-TEXAS-3` tenant:

| HOSTNAME      | OS	   | IP ADDRESS	 | RAM	| VCPU	| FLAVOUR	| KEY |
| ------------- |--------- | ----------- | ---- | ----- | ------------- | --- |
| devhost   | CentOS-6 | 10.100.0.47, 128.107.1.243 | 8	| 2	| GP2-Large	| development |

**Installed tools:**
- Oracle JDK 1.7
- Maven 
- Git 
- Ansible
- Pip
- Swift Client
- Nova Client
- Neutron Client
- Cloudera libraries

**How to access the development VM:**
- via SSH: `ssh cloud-user@128.107.1.243 -i development.pem` (via Cisco VPN) or `ssh cloud-user@10.100.0.47 -i development.pem` (if you are connected to the cluster's VPN)

## CDH cluster

The development CDH cluster is Cloudera managed CDH 5.4.8, Parcels (please see components versions below). 

**The nodes are:**

| HOSTNAME      | OS	   | IP ADDRESS	 | RAM	| VCPU	| FLAVOUR	| KEY |
| ------------- |--------- | ----------- | ---- | ----- | ------------- | --- |
| cdh-edge-01   | CentOS-6 | 10.100.0.40 | 8	| 2	| GP2-Large	| cdh | 
| cdh-master-01 | CentOS-6 | 10.100.0.45, 128.107.15.97 | 16	| 4	| GP2-Xlarge	| cdh |
| cdh-master-02 | CentOS-6 | 10.100.0.46 | 16	| 4	| GP2-Xlarge	| cdh |
| cdh-worker-04 | CentOS-6 | 10.100.0.39 | 32	| 8	| GP2-2Xlarge	| cdh |
| cdh-worker-01 | CentOS-6 | 10.100.0.43 | 32	| 8	| GP2-2Xlarge	| cdh |
| cdh-worker-03 | CentOS-6 | 10.100.0.41 | 32	| 8	| GP2-2Xlarge	| cdh |
| cdh-worker-02 | CentOS-6 | 10.100.0.38 | 32	| 8	| GP2-2Xlarge	| cdh | 
| cdh-worker-05 | CentOS-6 | 10.100.0.44 | 32	| 8	| GP2-2Xlarge	| cdh |
| cdh-worker-06 | CentOS-6 | 10.100.0.42 | 32	| 8	| GP2-2Xlarge	| cdh |

**Useful links are:**

* Cloudera Manager UI: [http://128.107.15.97:7180](http://128.107.15.97:7180), login/password: `admin/admin`
* NameNode UI: [http://128.107.15.97:50070](http://128.107.15.97:50070)
* Spark Master UI: [http://cdh-master-02.cisco.com:18080](http://cdh-master-02.cisco.com:18080)
* Spark History Server UI: [http://cdh-master-02.cisco.com:18088](http://cdh-master-02.cisco.com:18088)

**Currently installed components:**

- HDFS
- ZooKeeper
- Spark Standalone
- Kafka
- Solr

**CDH 5.4.8 goes with the below packages:**

| Component	| Package Version |
| ------------- | --------------- |
| Apache Avro	| avro-1.7.6+cdh5.4.8+97 |
| Apache Crunch	| crunch-0.11.0+cdh5.4.8+76 |
| DataFu	| pig-udf-datafu-1.1.0+cdh5.4.8+24 |
| Apache Flume	| flume-ng-1.5.0+cdh5.4.8+137 |
| Apache Hadoop	| hadoop-2.6.0+cdh5.4.8+669 |
| Apache HBase	| hbase-1.0.0+cdh5.4.8+195 |
| HBase-Solr	| hbase-solr-1.5+cdh5.4.8+60 |
| Apache Hive	| hive-1.1.0+cdh5.4.8+275 |
| Apache Kafka	| 0.8.2.0+kafka1.3.2+116 |
| Hue	| hue-3.7.0+cdh5.4.8+1303 |
| Cloudera Impala	| impala-2.2.0+cdh5.4.8+0 |
| Kite SDK	| kite-1.0.0+cdh5.4.8+41 |
| Llama	| llama-1.0.0+cdh5.4.8+0 |
| Apache Mahout	| mahout-0.9+cdh5.4.8+30 |
| Apache Oozie	| oozie-4.1.0+cdh5.4.8+152 |
| Parquet	| parquet-1.5.0+cdh5.4.8+99 |
| Parquet-format	| parquet-format-2.1.0+cdh5.4.8+15 |
| Apache Pig	| pig-0.12.0+cdh5.4.8+67 |
| Cloudera Search	| search-1.0.0+cdh5.4.8+0 |
| Apache Sentry (incubating)	| sentry-1.4.0+cdh5.4.8+183 |
| Apache Solr	| solr-4.10.3+cdh5.4.8+287 |
| Apache Spark	| spark-1.3.0+cdh5.4.8+51 |
| Apache Sqoop	| sqoop-1.4.5+cdh5.4.8+119 |
| Apache Sqoop2	| sqoop2-1.99.5+cdh5.4.8+40 |
| Apache Whirr	| whirr-0.9.0+cdh5.4.8+20 |
| Apache ZooKeeper	| zookeeper-3.4.5+cdh5.4.8+96 |

## Cassandra database

The Cassandra cluster is OpsCenter Cassandra 2.2.3 database. 

**The nodes are:**

| HOSTNAME      | OS	   | IP ADDRESS	 | RAM	| VCPU	| FLAVOUR	| KEY |
| ------------- |--------- | ----------- | ---- | ----- | ------------- | --- |
| cdh-worker-04 | CentOS-6 | 10.100.0.39 | 32	| 8	| GP2-2Xlarge	| cdh |
| cdh-worker-01 | CentOS-6 | 10.100.0.43 | 32	| 8	| GP2-2Xlarge	| cdh |
| cdh-worker-03 | CentOS-6 | 10.100.0.41 | 32	| 8	| GP2-2Xlarge	| cdh |
| cdh-worker-02 | CentOS-6 | 10.100.0.38 | 32	| 8	| GP2-2Xlarge	| cdh | 
| cdh-worker-05 | CentOS-6 | 10.100.0.44 | 32	| 8	| GP2-2Xlarge	| cdh |
| cdh-worker-06 | CentOS-6 | 10.100.0.42 | 32	| 8	| GP2-2Xlarge	| cdh |

**Useful links are:**

* OpsCenter UI: [http://128.107.15.97:7180](http://128.107.15.97:8888)

**How to connect to the Cassandra database:**

```
cqlsh <any-cassandra-node>
```
