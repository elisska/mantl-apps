package com.cisco.mantl.cassandra

import com.datastax.spark.connector._
import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.mutable.ArrayBuffer

/**
 * Created by dbort on 11.11.2015.
 */
object TableGen {

  def main(args: Array[String]) {

    Thread.sleep(5000)

    val sparkConfig = new SparkConf()
      .setAppName("TableGen")
      .setMaster("spark://quickstart.cloudera:7077")
      .set("spark.cassandra.connection.host", "10.100.0.43")
      .set("spark.cassandra.connection.port", "9042")

    val tabeleFQN = args(0).split("\\.")
    val sc = new SparkContext(sparkConfig)
    val table = sc.cassandraTable(tabeleFQN(0), tabeleFQN(1))
    val tableDef = table.tableDef
    val partitionKey = tableDef.partitionKey
    val clusteringCols = tableDef.clusteringColumns
    val regularCols = tableDef.regularColumns

    val data = new ArrayBuffer[String]
    for (i <- 1 to args(1).toInt){
      data.append("data-" + System.nanoTime().toString)
    }

    val columns = data.map(Tuple4("u1", 1, System.nanoTime(), _))

    sc.parallelize(columns)
      .saveToCassandra(tabeleFQN(0), tabeleFQN(1), SomeColumns("name" as "_1", "alias" as "_2", "count" as "_3", "data" as "_4"))

    println()



//    for(i <- 1 to args(1).toInt) {
//
//    }


  }



}
