package com.cisco.mantl.integration

import _root_.kafka.serializer.{StringDecoder, StringEncoder}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka._
import org.elasticsearch.spark.rdd.EsSpark


/**
 * Created by dbort on 09.10.2015.
 * Spark Streaming integration application.
 * Integrates Kafka broker and Elasticsearch by listening to Kafka topic, consuming messages and transferring them into ES index.
 */
object ItgDriver {

  def main(args: Array[String]) {

    //turn on debug - $ export SPARK_JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

    val parser = new scopt.OptionParser[Config](MsgUsage) {
      head("Spark Integration", "1.0")
      opt[String]('b', "brokers") required() valueName ("<brokers>") action { (x, c) =>
        c.copy(brokers = x.trim)
      } text (MsgBrokers)
      opt[String]('t', "topics") required() valueName ("<topics>") action { (x, c) =>
        c.copy(topics = x.trim)
      } text (MsgTopics)
      opt[String]('h', "host") valueName ("<esHost>") action { (x, c) =>
        c.copy(esHost = x.trim)
      } validate { x => if (x.split(":").size > 1) success else failure("Value of --host must conform to format <hostname>:<port>")
      } text (MsgHost)
      opt[String]('i', "index") required() valueName ("<esIndex>") action { (x, c) =>
        c.copy(esIndex = x.trim)
      } text (MsgIndex)
      opt[String]('s', "type") required() valueName ("<esType>") action { (x, c) =>
        c.copy(esType = x.trim)
      } text (MsgType)
      opt[String]('m', "master") valueName ("<masterURI>") action { (x, c) =>
        c.copy(master = x.trim)
      } text (MsgMaster)
      opt[String]('n', "name") valueName ("<appName>") action { (x, c) =>
        c.copy(name = x.trim)
      } text (MsgName)
      opt[Long]('c', "batchint") valueName ("<batchInterval>") action { (x, c) =>
        c.copy(batchInterval = x)
      } text (MsgBatchInterval)
      help("help") text (MsgHelp)
      note(MsgNote)
    }

    parser.parse(args, Config()) match {
      case Some(config) =>

        val sparkConf = new SparkConf()
        if(!config.name.isEmpty)sparkConf.setAppName(config.name)
        if(!config.master.isEmpty)sparkConf.setMaster(config.master)

        val ssc = new StreamingContext(sparkConf, Seconds(config.batchInterval))
        val esHostInfo = config.esHost.split(":")
        ssc.putJson(ssc.consume(config.brokers, config.topics), esHostInfo(0), esHostInfo(1), config.esIndex, config.esType)

        ssc.start()
        ssc.awaitTermination()
      case None => println("ERROR: bad argument set provided")
    }
  }

  /**
   * Provides additional methods via implicit conversion for StreamingContext in order to commence consuming broker messages and transferring them into ES index.
   * @param origin
   */
  implicit class IntegrationUtils(val origin: StreamingContext) {

    def consume(brokers: String, topics: String): InputDStream[(String, String)] = {
      val topicsSet = topics.split(",").toSet
      val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers, "serializer.class" -> classOf[StringEncoder].getName, "auto.offset.reset" -> "smallest")
      KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](origin, kafkaParams, topicsSet)
    }

    def putJson(messages: InputDStream[(String, String)], esHost: String, esPort: String, esIndex: String, esType: String): Unit = {
      val lines = messages.map(_._2)
      lines.print()
      val esParams = Map[String, String]("es.nodes" -> esHost, "es.port" -> esPort)
      lines.foreachRDD(rdd => EsSpark.saveJsonToEs(rdd, esIndex + "/" + esType, esParams))
    }
  }

}

/**
 * Holder class for local CLI configuration.
 * @param brokers
 * @param topics
 * @param esHost
 * @param esIndex
 * @param esType
 * @param master
 * @param name
 * @param batchInterval
 */
case class Config(brokers: String = "",
                  topics: String = "",
                  esHost: String = "localhost:9200",
                  esIndex: String = "",
                  esType: String = "",
                  master: String = "",
                  name: String = "Spark Integration",
                  batchInterval: Long = 1)
