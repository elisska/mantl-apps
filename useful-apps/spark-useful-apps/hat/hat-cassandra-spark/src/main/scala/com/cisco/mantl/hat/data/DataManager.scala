package com.cisco.mantl.hat.data

import com.datastax.spark.connector.CassandraRow
import org.apache.spark.rdd.RDD

/**
 * Created by dbort on 01.12.2015.
 */
object DataManager {

  def toDouble(data: RDD[CassandraRow]): RDD[Array[Double]] = {
    data.map(entry => Array[Double](entry.getDouble("acc_x"), entry.getDouble("acc_y"), entry.getDouble("acc_z")))
  }

  def withTimestamp(data: RDD[CassandraRow]): RDD[Array[Long]] = {
    data.map(entry => Array[Long](entry.getLong("timestamp"), entry.getDouble("acc_y").toLong))
  }

}
