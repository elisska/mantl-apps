package com.cisco.apps

import java.text.SimpleDateFormat
import java.util.Date



import kafka.serializer.StringDecoder
import org.apache.spark.rdd.RDD
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.SQLContext._

import org.elasticsearch.spark.sql._


object kafkamonitor {


    def main(args: Array[String]) {

      // Get command line arguments
      var Array(brokers, topics, cpu_threshold, ram_threshold, es_hosts, dest_es) = args

      // Specify runtime in ms
      val runtime = 300000

    // Creating Streaming context configuration, specifying ElasticSearch node addresses
    val sparkConf = new SparkConf().setAppName("KafkaMonitor")
      .set("es.nodes", es_hosts)
      .set("es.index.auto.create", "true")

    // Initialize SparkContext and Streaming context to read from Kafka
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Seconds(10))

    // Setup and initialize Kafka direct stream
    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String,String]("metadata.broker.list" -> brokers, "refresh.leader.backoff.ms" -> "5000", "group.id" -> "sparkmonitor", "auto.offset.reset" -> "largest")
    val lines = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc,kafkaParams,topicsSet).map(_._2)

    // initiate sql context
    val sqlContext = SQLContextSingleton.getInstance(sc)

    // Cycle for each message in a stream
    lines.foreachRDD((rdd: RDD[String], time: Time) => {
      // Working only on non-empty RDD
      if(rdd.count() > 0)
        {
          // Create dataframe from JSON
          // Check if our dataframe contains needed columns in order for further SQL queries to work and register temp table
          val df = sqlContext.jsonRDD(rdd).filter("type IN('collectd')").registerTempTable("metrics")

          // Check whether there are hosts that have metrics beyond threshold and write alarms to ElasticSearch index
          println("Hosts with average ram usage >= " + ram_threshold + "% and average CPU usage >= " + cpu_threshold + "%")
          sqlContext.sql("select host,MAX(`@timestamp`) AS date,plugin,AVG(value) AS metric from metrics " +
                                                  "where (plugin IN ('memory','cpu') " +
                                                  "AND collectd_type = 'percent' " +
                                                  "AND type_instance IN('free','idle')) " +
                                                  "GROUP BY host,plugin HAVING " +
                                                  "(CASE " +
                                                  "WHEN plugin='cpu' THEN metric <=(100-" + cpu_threshold + ") " +
                                                  "WHEN plugin='mem' THEN metric <=(100-" + ram_threshold + ") " +
                                                  "END)").saveToEs(dest_es)


    }
    })

    // Start streaming context to read from Kafka, work for 5 minutes and then terminate
    ssc.start()
    ssc.awaitTerminationOrTimeout(runtime)

  }
}

/** Case class for converting RDD to DataFrame */
case class Record(word: String)

/** Lazily instantiated singleton instance of SQLContext */
object SQLContextSingleton {
  @transient  private var instance: SQLContext = _
  def getInstance(sparkContext: SparkContext): SQLContext = {
    if (instance == null) {
      instance = new SQLContext(sparkContext)
    }
    instance
  }
}