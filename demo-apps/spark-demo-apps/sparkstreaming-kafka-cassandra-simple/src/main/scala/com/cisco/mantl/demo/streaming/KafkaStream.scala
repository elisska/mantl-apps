package com.cisco.mantl.demo.streaming

import java.lang.System
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.SparkConf

import com.datastax.spark.connector._
import com.datastax.spark.connector.streaming._

/**
 * Demo Kafka-Spark Streaming application
 * Reads data from Kafka topic and writes to Cassandra table
 */
object KafkaStream {

  def main(args: Array[String]) {

    val defaultConfig = Config()

    val parser = new scopt.OptionParser[Config]("com.cisco.spark.streaming.KafkaStream") {
      opt[String]('z', "zkQuorum") required() valueName("<host>:<port>") action { (x, c) => c.copy(zkQuorum = x)} text("zookeeper quorum, comma-separated list of host:port")
      opt[String]('g', "group") required() valueName("<consumer group name>") action { (x, c) => c.copy(group = x) } text("name of kafka consumer group")
      opt[String]('t', "topics") required() valueName("<kafka topic name>") action { (x, c) => c.copy(topics = x) } text("list of one or more kafka topics to consume from")
      opt[Int]('n', "numThreads") required() valueName("<number of threads>") action { (x, c) => c.copy(numThreads = x) } text("number of threads the kafka consumer should use")
      opt[String]('u', "url") valueName("<spark master url>") action { (x, c) => c.copy(url = x)} text("Spark master url to run locally")
      opt[String]('a', "address") action { (x, c) => c.copy(address = x)} text ("Cassandra node IP or FQDN, 127.0.0.1 by default")
      opt[Int]('p',"port") action { (x, c) => c.copy(port = x)} text ("Cassandra node native transport port, usually 9042")
    }

    parser.parse(args, defaultConfig) map { config =>
      run(config)
    } getOrElse {
      System.exit(1)
    }

    def run(config: Config) {

        StreamingExamples.setStreamingLogLevels()

        val sparkConf = new SparkConf().setAppName("KafkaStream")
                                        .set("spark.cassandra.connection.host", config.address)
                                        .set("spark.cassandra.connection.native.port", config.port.toString())

        val isLocalRun = (config.url.length() > 5);

        if (isLocalRun) {
          sparkConf.setMaster(config.url)
        }

        val ssc = new StreamingContext(sparkConf, Seconds(2))
        ssc.checkpoint("checkpoint")

        val topicMap = config.topics.split(",").map((_, config.numThreads.toInt)).toMap

        // String to concatenate with each message from Kafka - topic-name:timestamp
        val strToConcat = config.topics.concat(":").concat(System.currentTimeMillis().toString()).concat(":")

        // Creating stream from Kafka
        var lines = KafkaUtils.createStream(ssc, config.zkQuorum, config.group, topicMap).map(_._2)

        // Concatenating strToConcat with each message from Kafka stream
        var conc = lines.map(x => strToConcat.concat(x))
	
	// try-catch to be added here
	conc.map(Tuple1(_)).saveToCassandra("streamingsample", "streaming_sample", SomeColumns("streammsg"))

        ssc.start()
        ssc.awaitTermination()
    }
  }
}
