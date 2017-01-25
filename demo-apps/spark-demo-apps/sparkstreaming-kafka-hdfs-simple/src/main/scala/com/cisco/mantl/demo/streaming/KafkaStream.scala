package com.cisco.mantl.demo.streaming

import java.lang.System
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.SparkConf

/**
 * Demo Kafka-Spark Streaming application
 */
object KafkaStream {

  def main(args: Array[String]) {

    val defaultConfig = Config()

    val parser = new scopt.OptionParser[Config]("com.cisco.spark.streaming.KafkaStream") {
      opt[String]('z', "zkQuorum") required() valueName ("<host>:<port>") action { (x, c) => c.copy(zkQuorum = x) } text ("zookeeper quorum, comma-separated list of host:port")
      opt[String]('g', "group") required() valueName ("<consumer group name>") action { (x, c) => c.copy(group = x) } text ("name of kafka consumer group")
      opt[String]('t', "topics") required() valueName ("<kafka topic name>") action { (x, c) => c.copy(topics = x) } text ("list of one or more kafka topics to consume from")
      opt[Int]('n', "numThreads") required() valueName ("<number of threads>") action { (x, c) => c.copy(numThreads = x) } text ("number of threads the kafka consumer should use")
      opt[String]('o', "outDir") valueName ("<dir>") action { (x, c) => c.copy(outDir = x) } text ("HDFS directory to store output")
    }

    parser.parse(args, defaultConfig) map { config =>
      run(config)
    } getOrElse {
      System.exit(1)
    }

    def run(config: Config) {

      StreamingExamples.setStreamingLogLevels()

      val sparkConf = new SparkConf().setAppName("KafkaStream")

      val ssc = new StreamingContext(sparkConf, Seconds(2))
      ssc.checkpoint("checkpoint")

      val topicMap = config.topics.split(",").map((_, config.numThreads.toInt)).toMap

      // Creating stream from Kafka
      var lines = KafkaUtils.createStream(ssc, config.zkQuorum, config.group, topicMap).map(_._2)

      lines.saveAsTextFiles(config.outDir)

      ssc.start()
      ssc.awaitTermination()
    }
  }
}
