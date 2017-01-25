package com.cisco.mantl.hat.data

import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer

/**
 * Created by dbort on 01.12.2015.
 */
object PrepareData {

  /**
   * identify Jump
   */
  def boundariesDiff(timestamps: RDD[Long], firstElement: Long, lastElement: Long): RDD[(Array[Long], Long)] = {
    val firstRDD = timestamps.filter(record => record > firstElement)
    val secondRDD = timestamps.filter(record => record < lastElement)

    // define periods of recording
    firstRDD.zip(secondRDD).map(pair => (Array[Long](pair._1, pair._2), pair._1 - pair._2))
  }

  def defineJump(tsBoundaries: RDD[(Array[Long], Long)]): RDD[(Long, Long)] = {
    tsBoundaries.filter(pair => pair._2 > 100000000).map(pair => (pair._1(1), pair._1(0)))
  }

  // (min, max)
  def defineInterval(tsJump: RDD[(Long, Long)], firstElement: Long, lastElement: Long, windows: Long): ArrayBuffer[Array[Long]] = {
    val flatten: Array[Long] = tsJump.flatMap(pair => Array(pair._1, pair._2)).sortBy(t => t, true, 1).collect
    val size = flatten.length // always even
    val results = new ArrayBuffer[Array[Long]]()
    if(size == 0){
      results.append(Array[Long](firstElement, lastElement, Math.round((lastElement - firstElement) / windows).asInstanceOf[Long]))
    } else {
      results.append(Array[Long](firstElement, flatten(0), Math.round((flatten(0) - firstElement) / windows).asInstanceOf[Long])) // init condition
      for(i <- 1 to size - 2 by 2){
        results.append(Array[Long](flatten(i), flatten(i + 1), Math.round((flatten(i + 1) - flatten(i)) / windows).asInstanceOf[Long]))
      }
      results.append(Array[Long](flatten(size - 1), lastElement, Math.round((lastElement - flatten(size - 1)) / windows).asInstanceOf[Long])) // end condition
    }
    results
  }

}
