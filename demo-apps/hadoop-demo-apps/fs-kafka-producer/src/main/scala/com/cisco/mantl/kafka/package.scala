package com.cisco.mantl

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

/**
 * Created by dbort on 20.10.2015.
 * Object to hold constants and utility methods.
 */
package object kafka {

  val MsgUsage = "hadoop jar *.jar com.cisco.mantl.kafka.KProdDriver"
  val MsgBrokers = "\"brokers\" is a required property, specify it as comma separated list to point out brokers messages will be produced to, example: broker1_host:port,broker2_host:port"
  val MsgTopic = "\"topic\" is a required property, specify it to point out topic to send to, example: topic1"
  val MsgInputDir = "\"inputDir\" is a required property, specify it to point out root directory files should be read from, example: hdfs://quickstart.cloudera:8020/user/examples/"
  val MsgZookeeper = "\"zkhost\" is an optional property, defines zookeeper host Kafka broker runs on, default: localhost:2181"
  val MsgHelp = "Use this option to check application usage"
  val MsgNote = "NOTE: arguments with spaces should be enclosed in \" \"."

  implicit def convertToRunnable[F](f: => F) = new Runnable() {
    def run() {
      f
    }
  }



}
