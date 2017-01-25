package com.cisco.mantl.hat.data

import org.apache.spark.mllib.stat.{Statistics, MultivariateStatisticalSummary}
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.{Vectors, Vector}

/**
 * Created by dbort on 01.12.2015.
 *
 * We use labeled accelerometer data from users thanks to a device in their pocket during different activities (walking, sitting, jogging, ascending stairs, descending stairs, and standing).
 *
 * The accelerometer measures acceleration in all three spatial dimensions as following:
 *
 * - Z-axis captures the forward movement of the leg
 * - Y-axis captures the upward and downward movement of the leg
 * - X-axis captures the horizontal movement of the leg
 *
 * After several tests with different features combination, the ones that I have chosen are described below:
 *
 * - Average acceleration (for each axis)
 * - Variance (for each axis)
 * - Average absolute difference (for each axis)
 * - Average resultant acceleration: 1/n * ∑ √(x² + y² + z²)
 * - Average time between peaks (max) (for each axis)
 *
 */
class ExtractFeature(data: RDD[Vector]) {

  private val summary: MultivariateStatisticalSummary = Statistics.colStats(data)

  /**
   * @return array (mean_acc_x, mean_acc_y, mean_acc_z)
   */
  def computeAvgAcc: Array[Double] = summary.mean.toArray

  /**
   * @return array (var_acc_x, var_acc_y, var_acc_z)
   */
  def computeVariance: Array[Double] = summary.variance.toArray

  /**
   * compute average time between peaks.
   */
  def computeAvgTimeBetweenPeak(data: RDD[Array[Long]]): Double = {
    // define the maximum
    val max: Array[Double] = summary.max.toArray

    // keep the timestamp of data point for which the value is greater than 0.9 * max
    // and sort it !
    val filtered_y: RDD[Long] = data.filter(record => record(1) > 0.9 * max(1)).map(record => record(0)).sortBy(time => time, true, 1)
    if(filtered_y.count() > 1){
      val firstElement = filtered_y.first
      val lastElement = filtered_y.sortBy(time => time, false, 1).first

      // compute the delta between each tick
      val firstRDD: RDD[Long] = filtered_y.filter(record => record > firstElement)
      val secondRDD: RDD[Long] = filtered_y.filter(record => record < lastElement)
      val product: RDD[Vector] = firstRDD.zip(secondRDD).map(pair => pair._1 - pair._2)
        // and keep it if the delta is != 0
        .filter(value => value > 0).map(Vectors.dense(_))

      // compute the mean of the delta
      Statistics.colStats(product).mean.toArray(0)
    }
    0.0
  }

}

object ExtractFeature {

  /**
   * @return array [ (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} ]
   */
  def computeAvgAbsDifference(data: RDD[Array[Double]], mean: Array[Double]): Array[Double] = {
    // for each point x compute x - mean
    // then apply an absolute value: |x - mean|
    val abs: RDD[Vector] = data.map(record => Array[Double](Math.abs(record(0) - mean(0)), Math.abs(record(1) - mean(1)), Math.abs(record(2) - mean(2)))).map(Vectors.dense(_))
    // And to finish apply the mean: for each axis (1 / n ) * ∑ |b - mean|
    Statistics.colStats(abs).mean.toArray
  }

  /**
   * @return Double resultant = 1/n * ∑ √(x² + y² + z²)
   */
  def computeResultantAcc(data: RDD[Array[Double]]): Double = {
    // first let's compute the square of each value and the sum
    // compute then the root square: √(x² + y² + z²)
    // to finish apply a mean function: 1/n * sum [√(x² + y² + z²)]
    val squared: RDD[Vector] = data.map(record => Math.pow(record(0), 2) + Math.pow(record(1), 2) + Math.pow(record(2), 2)).map(Math.sqrt(_)).map(sum => Vectors.dense(sum))

    Statistics.colStats(squared).mean.toArray(0)
  }

}
