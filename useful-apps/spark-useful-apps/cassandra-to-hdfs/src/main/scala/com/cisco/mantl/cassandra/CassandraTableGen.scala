package com.cisco.mantl.cassandra

import org.apache.spark.{Logging, SparkConf}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.scheduler.StreamingListener
import org.apache.spark.streaming.scheduler.StreamingListenerBatchCompleted
import com.datastax.spark.connector._

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
 * Created by dbort on 03.12.2015.
 */
object CassandraTableGen {

  def main(args: Array[String]) {

    //Thread.sleep(5000) //turn on debug - $ export SPARK_JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

    val parser = new scopt.OptionParser[CLIConfigGen](MsgUsageGen) {
      head(NameAppGen, "1.0")
      opt[String]('h', "host") required() valueName "<cassHost>" action { (x, c) =>
        c.copy(cassHost = x.trim)
      } validate { x => if (x.split(":").size == 2) success else failure("Value of --host must conform to format <hostname>:<port>")
      } text MsgHostGen
      opt[String]('t', "table") required() valueName "<cassTableFQN>" action { (x, c) =>
        c.copy(cassTableFQN = x.trim)
      } validate { x => if (x.split("\\.").size == 2) success else failure("Value of --table must conform to format <keyspace>.<table>")
      } text MsgTableGen
      opt[Int]('a', "amount") required() valueName "<dataAmount>" action { (x, c) =>
        c.copy(amount = x)
      } validate { x => if (x > 0 || x < Int.MaxValue / Mb) success else failure("Value of --amount must be > 0 and < " + Int.MaxValue / Mb)
      } text MsgAmountGen
      opt[Int]('f', "frequency") valueName "<batchFrequency>" action { (x, c) =>
        c.copy(frequency = x)
      } validate { x => if (x > 0) success else failure("Value of --frequency must be > 0")
      } text MsgFrqGen
      opt[Int]('s', "batchsize") valueName "<batchSize>" action { (x, c) =>
        c.copy(batchSize = x)
      } validate { x => if (x > 0) success else failure("Value of --batchsize must be > 0")
      } text MsgBatchSizeGen
      opt[Long]('b', "blocksize") valueName "<blockSize>" action { (x, c) =>
        c.copy(blockSize = x)
      } validate { x => if (x > 0) success else failure("Value of --blocksize must be > 0")
      } text MsgHdfsBlockSizeGen
      opt[String]('m', "master") valueName "<masterURI>" action { (x, c) =>
        c.copy(master = x.trim)
      } text MsgMasterGen
      opt[String]('n', "name") valueName "<appName>" action { (x, c) =>
        c.copy(name = x.trim)
      } text MsgNameGen
      opt[Boolean]('p', "print") valueName "<printBatch>" action { (x, c) =>
        c.copy(print = x)
      } text MsgPrintGen
      help("help") text MsgHelpGen
      note(MsgNoteGen)
    }

    parser.parse(args, CLIConfigGen()) match {
      case Some(cliConfigGen) =>

        val cassHost = cliConfigGen.cassHost.split(":")
        val tblFQN = cliConfigGen.cassTableFQN.split("\\.")


        val sparkConfig = new SparkConf()
          .set("spark.cassandra.connection.host", cassHost(0))
          .set("spark.cassandra.connection.port", cassHost(1))
        if (!cliConfigGen.name.isEmpty) sparkConfig.setAppName(cliConfigGen.name)
        if (!cliConfigGen.master.isEmpty) sparkConfig.setMaster(cliConfigGen.master)

        val ssc = new StreamingContext(sparkConfig, Seconds(cliConfigGen.frequency))
        if (cliConfigGen.blockSize > 0) ssc.sparkContext.hadoopConfiguration.setLong("dfs.blocksize", cliConfigGen.blockSize * Mb)

        ssc.addStreamingListener(new JobListener(ssc, cliConfigGen.amount * Mb, cliConfigGen.batchSize * Mb))
        val lines = ssc.receiverStream(new HATGenReceiver(cliConfigGen.amount * Mb, cliConfigGen.batchSize * Mb, cliConfigGen.frequency * 1000 + 500)) //provide a 0.5 second gap between data consuming and data generation

        if (cliConfigGen.print) lines.print()
        lines.foreachRDD(rdd => rdd.saveToCassandra(tblFQN(0), tblFQN(1),
          SomeColumns(
            "user_id" as "id",
            "activity" as "activity",
            "timestamp" as "timestamp",
            "acc_x" as "acceleration_x",
            "acc_y" as "acceleration_y",
            "acc_z" as "acceleration_z"))) //saveAsTextFile(outDir + System.currentTimeMillis().toString))

        ssc.start()
        ssc.awaitTermination()

      case None => println("ERROR: bad argument set provided")
    }
  }

  def generateModelBatch(recordsNum: Long = 10, userDiversity: Array[String] = Array[String]("1"), sorted: Boolean = false): ArrayBuffer[Model] = {

    val activities = Activity.values.toArray
    val activitiesSize = activities.length
    val random = new Random

    def getRandomSign: Int = {
      if (random.nextInt() % 2 == 0) 1 else -1
    }

    //var baseAcceleration: Double = random.nextInt(10000)
    var baseAccelerationXWalking: Double = 0
    var baseAccelerationXJoggling: Double = 0
    var baseAccelerationXUpstairs: Double = 0
    var baseAccelerationXDownstairs: Double = 0

    var baseAccelerationYWalking: Double = 0
    var baseAccelerationYJoggling: Double = 0
    var baseAccelerationYUpstairs: Double = 0
    var baseAccelerationYDownstairs: Double = 0

    var baseAccelerationZWalking: Double = 0
    var baseAccelerationZJoggling: Double = 0
    var baseAccelerationZUpstairs: Double = 0
    var baseAccelerationZDownstairs: Double = 0

    //    def getRandomAcceleration(): Double = {
    //      //random.nextDouble * 20 - random.nextDouble * 20
    //      baseAcceleration += 1
    //      baseAcceleration
    //    }

    def getAccX(activity: Activity.Value): Double = {
      //acceleration X data simulation in approximation to graphs on http://www.duchess-france.org/analyze-accelerometer-data-with-apache-spark-and-mllib/
      if (activity == Activity.Sitting) {
        6.5 + (6.5 / (50 + random.nextInt(50)) * getRandomSign)
      } else if (activity == Activity.Standing) {
        -0.5 + (-0.5 / (50 + random.nextInt(50)) * getRandomSign)
      } else if (activity == Activity.Walking) {
        baseAccelerationXWalking += 1
        Math.sin(baseAccelerationXWalking) * (1 + random.nextInt(5)) + (0 + random.nextInt(3) * getRandomSign)
      } else if (activity == Activity.Jogging) {
        baseAccelerationXJoggling += 1
        Math.sin(baseAccelerationXJoggling) * (1 + random.nextInt(8)) + (-1 + random.nextInt(3) * getRandomSign)
      } else if (activity == Activity.Upstairs) {
        baseAccelerationXUpstairs += 1
        Math.sin(baseAccelerationXUpstairs) * (1 + random.nextInt(4)) + (0 + random.nextInt(2) * getRandomSign)
      } else if (activity == Activity.Downstairs) {
        baseAccelerationXDownstairs += 1
        Math.sin(baseAccelerationXDownstairs) * (1 + random.nextInt(4)) + (0 + random.nextInt(2) * getRandomSign)
      } else {
        0
      }
    }

    def getAccY(activity: Activity.Value): Double = {
      //acceleration Y data simulation in approximation to graphs on http://www.duchess-france.org/analyze-accelerometer-data-with-apache-spark-and-mllib/
      if (activity == Activity.Sitting) {
        2.5 + (2.5 / (50 + random.nextInt(50)) * getRandomSign)
      } else if (activity == Activity.Standing) {
        10 + (10 / (50 + random.nextInt(50)) * getRandomSign)
      } else if (activity == Activity.Walking) {
        baseAccelerationYWalking += 1
        Math.sin(baseAccelerationYWalking) * (1 + random.nextInt(8)) + (7 + random.nextInt(3) * getRandomSign)
      } else if (activity == Activity.Jogging) {
        baseAccelerationYJoggling += 1
        Math.sin(baseAccelerationYJoggling) * (1 + random.nextInt(10)) + (5 + random.nextInt(3) * getRandomSign)
      } else if (activity == Activity.Upstairs) {
        baseAccelerationYUpstairs += 1
        Math.sin(baseAccelerationYUpstairs) * (1 + random.nextInt(8)) + (7 + random.nextInt(3) * getRandomSign)
      } else if (activity == Activity.Downstairs) {
        baseAccelerationYDownstairs += 1
        Math.sin(baseAccelerationYDownstairs) * (1 + random.nextInt(6)) + (random.nextInt(3) * getRandomSign)
      } else {
        0
      }
    }

    def getAccZ(activity: Activity.Value): Double = {
      //acceleration Z data simulation in approximation to graphs on http://www.duchess-france.org/analyze-accelerometer-data-with-apache-spark-and-mllib/
      if (activity == Activity.Sitting) {
        6.5 + (6.5 / (50 + random.nextInt(50)) * getRandomSign)
      } else if (activity == Activity.Standing) {
        -0.5 + (-0.5 / (50 + random.nextInt(50)) * getRandomSign)
      } else if (activity == Activity.Walking) {
        baseAccelerationZWalking += 1
        Math.sin(baseAccelerationZWalking) * (1 + random.nextInt(7)) + (3 + random.nextInt(2) * getRandomSign)
      } else if (activity == Activity.Jogging) {
        baseAccelerationZJoggling += 1
        Math.sin(baseAccelerationZJoggling) * (1 + random.nextInt(8)) + (-2 + random.nextInt(3) * getRandomSign)
      } else if (activity == Activity.Upstairs) {
        baseAccelerationZUpstairs += 1
        Math.sin(baseAccelerationZUpstairs) * (1 + random.nextInt(5)) + (2 + random.nextInt(2) * getRandomSign)
      } else if (activity == Activity.Downstairs) {
        baseAccelerationZDownstairs += 1
        Math.sin(baseAccelerationZDownstairs) * (1 + random.nextInt(5)) + (2 + random.nextInt(3) * getRandomSign)
      } else {
        0
      }
    }

    def getSampling(base: Long): Long = {
      base + (base / (50 + random.nextInt(50)) * getRandomSign) + (base / (100 + random.nextInt(100)) * getRandomSign) //base +- 3%
    }

    def getBaseTimestampModifier: Long = System.nanoTime() / (1 + random.nextInt(10)) //assumes one batch of data mined by different device during continuous period, http://www.cis.fordham.edu/wisdm/dataset.php
    def getWindowSize(base: Int): Int = {
      950 + random.nextInt(100)
    }
    def getIntraSamplingPeriod: Long = getSampling(50000000)
    //def getInterSamplingPeriod: Long = getSampling(100000000)
    def getInterSamplingPeriod: Long = 100000001

    def getModel(user: String, activity: Activity.Value, timestamp: String): Model = {
      new Model(user,
        activity.toString,
        timestamp,
        getAccX(activity).toString,
        getAccY(activity).toString,
        getAccZ(activity).toString)
    }


    def calcRecsPerDiversity(recsNum: Long, diversity: Long): Long = {
      (recsNum / (diversity.toDouble + diversity.toDouble / (5 + (random.nextInt(6))) * getRandomSign)) toLong // recs/diversity +- 10%
    }


    def appendGenData(recNumPerUser: Long, name: String, buffer: ArrayBuffer[Model]): Unit = {


      var baseTimestampMod = getBaseTimestampModifier

      val walkingRecs = calcRecsPerDiversity(recNumPerUser, activitiesSize)
      val joglingRecs = calcRecsPerDiversity(recNumPerUser, activitiesSize)
      val sittingRecs = calcRecsPerDiversity(recNumPerUser, activitiesSize)
      val standingRecs = calcRecsPerDiversity(recNumPerUser, activitiesSize)
      val upstairsRecs = calcRecsPerDiversity(recNumPerUser, activitiesSize)
      val downstairsRecs = recNumPerUser - (walkingRecs + joglingRecs + sittingRecs + standingRecs + upstairsRecs)

      appendSingleActivity(buffer, walkingRecs, name, Activity.Walking)
      appendSingleActivity(buffer, joglingRecs, name, Activity.Jogging)
      appendSingleActivity(buffer, sittingRecs, name, Activity.Sitting)
      appendSingleActivity(buffer, standingRecs, name, Activity.Standing)
      appendSingleActivity(buffer, upstairsRecs, name, Activity.Upstairs)
      appendSingleActivity(buffer, downstairsRecs, name, Activity.Downstairs)

      def appendSingleActivity(buffer: ArrayBuffer[Model], recNum: Long, name: String, activity: Activity.Value): Unit = {
        if (recNum > 0) {
          for (rec <- 1 to recNum.toInt) {
            if (rec % 1000 == 0) baseTimestampMod += getInterSamplingPeriod //increate for > 100 mills to indicate sampling window break to create a window aout of every 1000 recs
            else baseTimestampMod += getIntraSamplingPeriod //increase timestamp for 50 mills to indicate base 20Hz sampling
            buffer.append(getModel(name, activity, baseTimestampMod.toString))
          }
        }
      }
    }

    val data = new ArrayBuffer[Model]
    for (userIndex <- 0 to userDiversity.length - 1) {
      if (userIndex != userDiversity.length - 1) {
        val baseRecNumPerUser = calcRecsPerDiversity(recordsNum, userDiversity.length)
        appendGenData(baseRecNumPerUser, userDiversity(userIndex), data)
      } else {
        val baseRecNumPerUser = recordsNum - data.size
        if (baseRecNumPerUser > 0) {
          appendGenData(baseRecNumPerUser, userDiversity(userIndex), data)
        }
      }
    }
    data
    //if (sorted) data.sortWith(_ < _)
  }

}

class JobListener(ssc: StreamingContext, expectedBytesAll: Long, expectedBytesBatch: Long) extends StreamingListener {

  private var batchQty: Long = ((expectedBytesAll / expectedBytesBatch) * 1.3 + 2) toLong //calculate number of stream batches to be processed before shutdown

  override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted): Unit = {
    if (batchQty < 0) {
      //      val aggRDD = ssc.sparkContext.combineTextFiles("hdfs://localhost/user/examples1/files-out", 128, 128, "\n", "true")
      //      aggRDD.saveAsTextFile("hdfs://localhost/user/examples1/files-out/agg")
      ssc.stop(true, false)
    }
    batchQty = batchQty - 1
  }

}

class HATGenReceiver(expectedBytesAll: Long, expectedBytesBatch: Long, freq: Long) extends Receiver[Model](StorageLevel.MEMORY_ONLY) with Logging {

  override def onStart(): Unit = {
    new Thread("Human Activity Generator Receiver") {
      override def run() {
        val currentRecordSizeAvg = CassandraTableGen.generateModelBatch().head.getBytesCount //calculate approximate number of bytes in one record
        val batchQty: Int = (expectedBytesAll / expectedBytesBatch) toInt//calculate total number of batches
        val recordsPerBatch: Long = expectedBytesBatch / currentRecordSizeAvg //calculate number of records per batch
        for (user <- 1 to batchQty) {
          store(CassandraTableGen.generateModelBatch(recordsPerBatch, Array[String](user.toString)))
          Thread.sleep(freq)
        }
      }
    }.start()
  }

  override def stop(message: String): Unit = {
    logInfo(message)
  }

  override def onStop(): Unit = {
    logInfo("------Human Activity Generator ceased------") //tbd
  }

}

object Activity extends Enumeration {
  type Activity = Value
  val Walking, Jogging, Sitting, Standing, Upstairs, Downstairs = Value
}

@SerialVersionUID(111L)
class Model(val id: String,
            val activity: String,
            val timestamp: String,
            val acceleration_x: String,
            val acceleration_y: String,
            val acceleration_z: String) extends Ordered[Model] with Serializable {

  override def compare(that: Model): Int = {
    val i = this.id.compareTo(that.id)
    if (i != 0) i else this.activity.compareTo(that.activity)
  }

  def getBytesCount = toString.getBytes.size

  override def toString = s"$id,$activity,$timestamp,$acceleration_x,$acceleration_y,$acceleration_z"
}

case class CLIConfigGen(cassHost: String = "",
                        cassTableFQN: String = "",
                        amount: Long = 10,
                        frequency: Long = 3,
                        batchSize: Long = 5 * Mb,
                        blockSize: Long = 0,
                        master: String = "",
                        name: String = NameApp,
                        print: Boolean = false)
