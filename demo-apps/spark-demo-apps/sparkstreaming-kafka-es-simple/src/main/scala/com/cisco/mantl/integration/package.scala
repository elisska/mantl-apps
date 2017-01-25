package com.cisco.mantl

/**
 * Created by dbort on 13.10.2015.
 * Utility object to hold constants.
 */
package object integration {

  val MsgUsage = "spark-submit --class \"com.cisco.mantl.integration.ItgDriver\" *.jar"
  val MsgBrokers = "\"brokers\" is a required property, specify it as comma separated list to point out brokers Kafka stream to be created on, example: broker1_host:port,broker2_host:port"
  val MsgHost = "\"host\" is a optional property, specify it to point out Elasticsearch host, default: localhost:9200"
  val MsgTopics = "\"topics\" is a required property, specify it as comma separated list to point out topics to consume from, example: topic1,topic2"
  val MsgIndex = "\"index\" is a required property, specify it to point out Elasticsearch index, example: someindex"
  val MsgType = "\"type\" is a required property, specify it to point out Elasticsearch type, example: sometype"
  val MsgMaster = "\"master\" is a optional property, specify it to point out spark master URI, example: spark://quickstart.cloudera:7077  default: governed by --master option of spark-submit command which would be required in case not providing in to Spark Integration application"
  val MsgName = "\"name\" is an optional property, Application display name, default: Spark Integration"
  val MsgBatchInterval = "\"batchint\" is an optional property, defines duration in seconds between consuming topic messages, default: 1"
  val MsgHelp = "Use this option to check application usage"
  val MsgNote = "NOTE: arguments with spaces should be enclosed in \" \"."

}
