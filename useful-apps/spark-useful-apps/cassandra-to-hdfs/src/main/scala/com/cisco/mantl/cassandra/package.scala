package com.cisco.mantl

/**
 * Created by dbort on 12.11.2015.
 */
package object cassandra {

  val NameApp = "Cassandra-to-HDFS"
  val MsgUsage = "spark-submit --class \"com.cisco.mantl.cassandra.CHDriver\" --master spark://<host>:<port> *.jar"

  val MsgHost = "\"host\" is a required property, specify it to point out Cassandra host, example: localhost:9042"
  val MsgTable = "\"table\" is a required property, specify it to point out Cassadnra table full name including keyspace, example: keyspace.table"
  val MsgOut = "\"out\" is a required property, specify it to point out directory URI to upload cassandra table contents, example: hdfs://localhost/user/examples/files-out"
  val MsgHdfsBlockSize = "\"bsize\" is an optional property, HDFS file block size in Mb, default: governed by dfs.blocksize Hadoop option"
  val MsgMaster = "\"master\" is a optional property, specify it to point out spark master URI, example: spark://localhost:7077  default: governed by --master option of spark-submit command which would be required in case not providing in to " + NameApp + " application"
  val MsgName = "\"name\" is an optional property, Application display name, default: " + NameApp

  val MsgHelp = "Use this option to check application usage"
  val MsgNote = "NOTE: arguments with spaces should be enclosed in \" \"."

  val Mb = 1024 * 1024

  val NameAppGen = "HAT-data-generator"
  val MsgUsageGen = "spark-submit --class \"com.cisco.mantl.hat.HATGenDriver\" --master spark://<host>:<port> *.jar"

  val MsgHostGen = "\"host\" required property, Cassandra host, example: localhost:9042"
  val MsgTableGen = "\"table\" required property, Cassadnra table full name including keyspace, example: keyspace.table"
  val MsgOutGen = "\"out\" required property, directory URI to upload generator output, example: hdfs://localhost/user/examples/files-out"
  val MsgAmountGen = "\"amount\" required property, amount of data in megabytes to be generated, example: 1024"
  val MsgFrqGen = "\"frequency\" optional property, batch frequency in seconds, default is set to 3 seconds, example: 5"
  val MsgBatchSizeGen = "\"batchsize\" optional property, amount of data in megabytes to be generated per batch, affects number of output directories, default: 1, example: 1"
  val MsgHdfsBlockSizeGen = "\"blocksize\" optional property, HDFS file block size in Mb, default: governed by dfs.blocksize Hadoop option"

  val MsgMasterGen = "\"master\" optional property, spark master URI, example: spark://localhost:7077  default: governed by --master option of spark-submit command which would be required in case not providing in to " + NameAppGen + " application"
  val MsgNameGen = "\"name\" optional property, Application display name, default: " + NameAppGen
  val MsgPrintGen = "\"print\" optional property, enable/disable batch contents to be printed to console output, default: false"

  val MsgHelpGen = "Use this option to check application usage"
  val MsgNoteGen = "NOTE: arguments with spaces should be enclosed in \" \"."

}
