package com.cisco.mantl

/**
 * Created by dbort on 24.11.2015.
 */
package object hat {

  val NameApp = "HAT-cassandra-spark"
  val MsgUsage = "spark-submit --class \"com.cisco.mantl.hat.HATDriver\" --master spark://<host>:<port> *.jar"

  val MsgHost = "\"host\" is a required, Cassandra host, example: localhost:9042"
  val MsgTable = "\"table\" is a required, Cassadnra table full name including keyspace, example: keyspace.table"
  val MsgFunction = "\"function\"" + s"is a required, machine learning function, possible values: $FuncPredict, $FuncRecognize, example: $FuncRecognize"
  val MsgUsers = "\"users\" is a required, number of users to operate on, example: 20"

  val MsgMaster = "\"master\" optional property, spark master URI, example: spark://localhost:7077  default: governed by --master option of spark-submit command which would be required in case not providing in to " + NameApp + " application"
  val MsgName = "\"name\" optional property, Application display name, default: " + NameApp

  val MsgHelp = "Use this option to check application usage"
  val MsgNote = "NOTE: arguments with spaces should be enclosed in \" \"."

  val Mb = 1024 * 1024

  val FuncPredict = "predict"
  val FuncRecognize = "recognize"

}
