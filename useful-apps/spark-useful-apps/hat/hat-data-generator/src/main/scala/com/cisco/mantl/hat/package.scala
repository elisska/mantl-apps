package com.cisco.mantl

/**
 * Created by dbort on 23.11.2015.
 */
package object hat {

  val NameApp = "HAT-data-generator"
  val MsgUsage = "spark-submit --class \"com.cisco.mantl.hat.HATGenDriver\" --master spark://<host>:<port> *.jar"

  val MsgOut = "\"out\" required property, directory URI to upload generator output, example: hdfs://localhost/user/examples/files-out"
  val MsgAmount = "\"amount\" required property, amount of data in megabytes to be generated, example: 1024"
  val MsgFrq = "\"frequency\" optional property, batch frequency in seconds, default is set to 3 seconds, example: 5"
  val MsgBatchSize = "\"batchsize\" optional property, amount of data in megabytes to be generated per batch, affects number of output directories, default: 1, example: 1"
  val MsgHdfsBlockSize = "\"blocksize\" optional property, HDFS file block size in Mb, default: governed by dfs.blocksize Hadoop option"

  val MsgMaster = "\"master\" optional property, spark master URI, example: spark://localhost:7077  default: governed by --master option of spark-submit command which would be required in case not providing in to " + NameApp + " application"
  val MsgName = "\"name\" optional property, Application display name, default: " + NameApp
  val MsgPrint = "\"print\" optional property, enable/disable batch contents to be printed to console output, default: false"

  val MsgHelp = "Use this option to check application usage"
  val MsgNote = "NOTE: arguments with spaces should be enclosed in \" \"."

  val Mb = 1024 * 1024

}
