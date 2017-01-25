package com.cisco.apps

import java.text.SimpleDateFormat
import java.util.Date

import kafka.serializer.StringDecoder
import org.apache.spark.rdd.RDD
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.sql.SQLContext

// Elasticsearch connector imports
import org.elasticsearch.spark.sql._

// Cassandra connector imports
import com.datastax.spark.connector._
import com.datastax.spark.connector.streaming._

object kafka2multiple {


  def main(args: Array[String]) {

    var Array(brokers, topics, dest_hdfs, es_hosts, dest_es, cassandra_hosts, cassandra_keyspace, cassandra_table) = args

    val runtime = 300000
    val counttime = 30000

    // Creating Streaming context configuration, specifying ElasticSearch node addresses, Cassandra hosts addresses
    val sparkConf = new SparkConf().setAppName("Kafka2Multiple")
      .set("es.nodes", es_hosts)
      .set("spark.cassandra.connection.host", cassandra_hosts)

    // Initialize SparkContext and Streaming context to read from Kafka each second
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Seconds(1))

    // Setup and initialize Kafka direct stream
    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String,String]("metadata.broker.list" -> brokers)
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc,kafkaParams,topicsSet)
    val lines = messages.map(_._2)

    var nonEmpty = false

    // Cycle for each line in a stream
    lines.foreachRDD((rdd: RDD[String], time: Time) => {
      // Working only on non-empty RDD
      if(rdd.count() > 0)
        {
          // Convert Direct Stream from Kafka to Data Frame
          val sqlContext = SQLContextSingleton.getInstance(rdd.sparkContext)
          import sqlContext.implicits._
          val linedf = rdd.map(w => Record(w)).toDF()

          // Write to Cassandra
          linedf.write.mode("append").format("org.apache.spark.sql.cassandra").options(Map( "table" -> cassandra_table, "keyspace" -> cassandra_keyspace)).save()

          // Write to HDFS
          linedf.write.mode("append").json(dest_hdfs)

          // Write to ElasticSearch
          linedf.saveToEs(dest_es)

          nonEmpty = true
        }
    })



    // Define thread to periodically count records in output destinations
    val countThread = new Thread(new Runnable {
      def run() {
        var count_runs = 0

        // Cycle to repeat thread each 30 seconds
        while(count_runs <= (runtime/counttime) ) {
          if (nonEmpty)
            {
              val hdfs_files_count = sc.textFile(dest_hdfs).partitions.length
              val sqlc = new SQLContext(sc)
              import sqlc.implicits._
              val es_records_count = sqlc.esDF(dest_es).count()
              val cassandra_records_count = sqlc.read.format("org.apache.spark.sql.cassandra").options(Map( "table" -> cassandra_table, "keyspace" -> cassandra_keyspace)).load().count()
              val curtime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss ").format(new Date())

              println("Time = " + curtime + " > HDFS Files = " + hdfs_files_count + " , ES records " + es_records_count + ", Cassandra records = " + cassandra_records_count)
            }
            { count_runs +=1; count_runs}
            Thread.sleep(counttime)
          }
      }
    })




    // Start streaming context to read from Kafka, work for 5 minutes and then terminate
    ssc.start()

    // Start counter thread
    countThread.start()

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