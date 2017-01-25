package com.cisco.mantl.hat
package model

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.rdd.RDD

import scala.collection.immutable.HashMap

/**
 * Created by dbort on 01.12.2015.
 */
class DecisionTrees(trainingData: RDD[LabeledPoint], testData: RDD[LabeledPoint]) {

  def createModel(sc: SparkContext): Double = {
    val categoricalFeaturesInfo = new HashMap[Int, Int]
    val numClasses = 6
    val impurity = "gini"
    val maxDepth = 9
    val maxBins = 32

    val model: DecisionTreeModel = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo, impurity, maxDepth, maxBins)
    model.save(sc, NameApp)

    // Evaluate model on training instances and compute training error
    val predictionAndLabel: RDD[(Double, Double)] = testData.map(p => (model.predict(p.features), p.label))
    val testErrDT: Double = 1.0 * predictionAndLabel.filter(pl => pl._1 != pl._2).count / testData.count
    testErrDT
  }

}
