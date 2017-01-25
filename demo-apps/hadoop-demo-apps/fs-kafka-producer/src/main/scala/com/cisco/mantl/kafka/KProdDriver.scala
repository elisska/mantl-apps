package com.cisco.mantl.kafka

import java.util.concurrent.Executors

import kafka.admin.AdminUtils
import kafka.api.TopicMetadata
import kafka.utils.ZKStringSerializer
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.serialize.ZkSerializer

import scala.concurrent._
import java.io.{FileNotFoundException, InputStreamReader, BufferedReader}
import java.util._
import org.apache.hadoop.fs._
import org.apache.hadoop.conf._
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import kafka.utils.ZkUtils._

/**
 * Created by dbort on 20.10.2015.
 * Kafka Producer application reads files from specified directory and forwards contents line by line into Kafka broker.
 * Each file is being read in a separate thread. Thread pool size depends on a target Kafka topic partition number property.
 */
object KProdDriver {

  def main(args: Array[String]) {

    val parser = new scopt.OptionParser[Config](MsgUsage) {
      head("Kafka Producer", "1.0")
      opt[String]('b', "brokers") required() valueName ("<brokers>") action { (x, c) =>
        c.copy(brokers = x.trim)
      } text (MsgBrokers)
      opt[String]('t', "topic") required() valueName ("<topic>") action { (x, c) =>
        c.copy(topic = x.trim)
      } text (MsgTopic)
      opt[String]('i', "inputDir") required() valueName ("<inputDir>") action { (x, c) =>
        c.copy(inputDir = x.trim)
      } text (MsgInputDir)
      opt[String]('z', "zkhost") valueName ("<zkHost>") action { (x, c) =>
        c.copy(zkHost = x)
      } text (MsgZookeeper)
      help("help") text (MsgHelp)
      note(MsgNote)
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        val core = new KProdCore(config.brokers, config.topic, new Path(config.inputDir), config.zkHost, null)
        core.run()

      case None => println("ERROR: bad argument set provided")
    }

  }

}

case class Config(brokers: String = "",
                  topic: String = "",
                  inputDir: String = "",
                  zkHost: String = "localhost:2181")


