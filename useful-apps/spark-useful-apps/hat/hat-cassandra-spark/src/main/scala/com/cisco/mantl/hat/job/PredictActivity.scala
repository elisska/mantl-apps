package com.cisco.mantl.hat
package job

import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.linalg.{Vectors, Vector}

/**
 * Created by dbort on 01.12.2015.
 */
object PredictActivity {

  def apply(sc: SparkContext): Unit = {
    println("PREDICTION: " + predict(sc))
  }

  def predict(sc: SparkContext): Double = {
    val model: DecisionTreeModel = DecisionTreeModel.load(sc, NameApp)
    //val feature: Array[Double] = Array[Double](3.3809183673469394, -6.880102040816324, 0.8790816326530612, 50.08965378708187, 84.13105050494424, 20.304453787081833, 5.930491461890875, 7.544194085797583, 3.519248229904206, 12.968485972481643, 7.50031E8)
    val feature: Array[Double] = Array[Double](-0.49953724403620237, 10.0, -0.5001992750707763, 5.614473254775297E-5, 0.0, 5.392670994771092E-5, 0.007310894129112669, 0.0, 0.007141189878656756, 10.024961122934215, 0.0)
    val sample: Vector = Vectors.dense(feature)
    val prediction: Double = model.predict(sample)
    prediction
  }

}
