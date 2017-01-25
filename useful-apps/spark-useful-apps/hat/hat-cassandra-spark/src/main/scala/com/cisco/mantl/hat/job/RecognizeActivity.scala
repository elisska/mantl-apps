package com.cisco.mantl.hat.job

import com.cisco.mantl.hat.data.{PrepareData, ExtractFeature, DataManager}
import com.cisco.mantl.hat.model.{RandomForests, DecisionTrees}
import com.datastax.spark.connector._
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.mutable.ArrayBuffer

/**
 * Created by dbort on 02.12.2015.
 */
object RecognizeActivity {

  def apply(sc: SparkContext, keyspace: String, table: String, usersToProcess: Int): Unit = {

    Thread.sleep(5000) //turn on debug - $ export SPARK_JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
    val startProcessing = System.currentTimeMillis();

    val cassandraRowsRDD = sc.cassandraTable(keyspace, table)
    val labeledPoints = new ArrayBuffer[LabeledPoint]()

    val activities = List("Standing", "Jogging", "Walking", "Sitting", "Upstairs", "Downstairs")

    for (user <- 1 to usersToProcess) {
      activities.foreach { // create bucket of sorted data by ascending timestamp by (user, activity)
        activity =>
          val times: RDD[Long] = cassandraRowsRDD.select("timestamp").where("user_id=? AND activity=?", user, activity).withAscOrder.
            map(cr => cr.toMap).
            map(entry => entry.get("timestamp").get.asInstanceOf[Long]).
            cache()

          if (times.count() > 100) {// if data
            val intervals: ArrayBuffer[Array[Long]] = defineWindows(times) //define the windows for each activity records intervals

            intervals.foreach {
              interval =>
                for (window <- 0 to interval(2).toInt - 1) {
                  val data: RDD[CassandraRow] = cassandraRowsRDD.select("timestamp", "acc_x", "acc_y", "acc_z").
                    where("user_id=? AND activity=? AND timestamp < ? AND timestamp > ?",
                      user, activity, interval(1) + window * 5000000000L, interval(1) + (window - 1) * 5000000000L).
                    withAscOrder.cache()
                  if(data.count() > 0) {

                    val doubles: RDD[Array[Double]] = DataManager.toDouble(data) // transform into double array

                    val vectors: RDD[Vector] = doubles.map(Vectors.dense(_)) // transform into vector without timestamp

                    val timestamp: RDD[Array[Long]] = DataManager.withTimestamp(data) // data with only timestamp and acc

                    val extractFeature = new ExtractFeature(vectors) //extract features from this windows

                    val mean: Array[Double] = extractFeature.computeAvgAcc // the average acceleration

                    val variance: Array[Double] = extractFeature.computeVariance // the variance

                    val avgAbsDiff: Array[Double] = ExtractFeature.computeAvgAbsDifference(doubles, mean) // the average absolute difference

                    val resultant: Double = ExtractFeature.computeResultantAcc(doubles) // the average resultant acceleration

                    val avgTimePeak: Double = extractFeature.computeAvgTimeBetweenPeak(timestamp) // the average time between peaks

                    // Let's build LabeledPoint, the structure used in MLlib to create and a predictive model
                    labeledPoints.append(getLabeledPoint(activity, mean, variance, avgAbsDiff, resultant, avgTimePeak))
                  }
                }
            }
          }
      }
    }

    if(labeledPoints.nonEmpty){ // ML part with the models: create model prediction and train data on it

      val data: RDD[LabeledPoint] = sc.parallelize(labeledPoints) // data ready to be used to build the model

      val splits: Array[RDD[LabeledPoint]] = data.randomSplit(Array[Double](0.6, 0.4)) // Split data into 2 sets : training (60%) and test (40%).

      val trainingData: RDD[LabeledPoint]  = splits(0).cache()

      val testData: RDD[LabeledPoint] = splits(1)

      val errDT: Double = new DecisionTrees(trainingData, testData).createModel(sc) // With DecisionTree

      val errRF: Double = new RandomForests(trainingData, testData).createModel() // With Random Forest

      val endProcessing = System.currentTimeMillis() - startProcessing

      println("sample size " + data.count())
      println("Test Error Decision Tree: " + errDT)

      println("Test Error Random Forest: " + errRF)
      println("Processing time: " + endProcessing / 1000 + " seconds")
    }

    sc.stop()

    def defineWindows(times: RDD[Long]): ArrayBuffer[Array[Long]] = {

      // first find jumps to define the continuous periods of data
      val firstElement = times.first()
      val lastElement = times.sortBy(t => t, false, 1).first()

      // compute the difference between each timestamp
      val tsBoundariesDiff: RDD[(Array[Long], Long)] = PrepareData.boundariesDiff(times, firstElement, lastElement)

      // define periods of recording
      // if the difference is greater than 100 000 000, it must be different periods of recording
      // ({min_boundary, max_boundary}, max_boundary - min_boundary > 100 000 000)
      val jumps: RDD[(Long, Long)] = PrepareData.defineJump(tsBoundariesDiff)

      // Now define the intervals
      PrepareData.defineInterval(jumps, firstElement, lastElement, 5000000000L)
    }

    /**
     * build the data set with label & features (11)
     * activity, mean_x, mean_y, mean_z, var_x, var_y, var_z, avg_abs_diff_x, avg_abs_diff_y, avg_abs_diff_z, res, peak_y
     */
    def getLabeledPoint(activity: String, mean: Array[Double], variance: Array[Double], avgAbsDiff: Array[Double], resultant: Double, avgTimePeak: Double): LabeledPoint = {
      // First the feature
      val features: Array[Double] = Array(mean(0), mean(1), mean(2), variance(0), variance(1), variance(2), avgAbsDiff(0), avgAbsDiff(1), avgAbsDiff(2), resultant, avgTimePeak)

      // Now the label: by default 0 for Walking
      var label: Double = 0
      if ("Jogging" == activity) {
        label = 1
      } else if ("Standing" == activity) {
        label = 2
      } else if ("Sitting" == activity) {
        label = 3
      } else if ("Upstairs" == activity) {
        label = 4
      } else if ("Downstairs" == activity) {
        label = 5
      }
      new LabeledPoint(label, Vectors.dense(features))
    }
  }

}
