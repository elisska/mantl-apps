package com.cisco.mantl.hat.model

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.rdd.RDD

import scala.collection.immutable.HashMap

/**
 * Created by dbort on 01.12.2015.
 */
class RandomForests(trainingData: RDD[LabeledPoint], testData: RDD[LabeledPoint]) {

  /**
   * Train a RandomForest model
   */
  def createModel(): Double = {
    val categoricalFeaturesInfo = new HashMap[Int, Int]
    val numTrees = 10
    val numClasses = 6
    val featureSubsetStrategy = "auto"
    val impurity = "gini"
    val maxDepth = 9
    val maxBins = 32

    // create model
    val model: RandomForestModel = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins, 12345);

    // Evaluate model on test instances and compute test error
    val predictionAndLabel: RDD[(Double, Double)] = testData.map(p => (model.predict(p.features), p.label))
    val testErrDT: Double = 1.0 * predictionAndLabel.filter(pl => pl._1 != pl._2).count / testData.count
    testErrDT


  }

}
