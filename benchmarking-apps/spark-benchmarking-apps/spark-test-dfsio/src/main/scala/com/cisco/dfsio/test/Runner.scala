package com.cisco.dfsio.test

import java.io.{FileWriter, BufferedWriter}
import java.lang.System._
import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.broadcast.Broadcast
import java.lang.System.{currentTimeMillis => _time}

/**
 * Created by vpryimak on 04.11.2015.
 */
object Runner extends App {

  // Method to profile a code block
  def profile[R](code: => R, t: Long = _time) = (code, _time - t)

  override def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[CliOptions]("--") {
      head("Spark DfsIO Test", "0.x")
      opt[String]('m', "mode") required() valueName ("read|write") action { (x, c) => c.copy(mode = x) } text ("execution mode: read or write")
      opt[String]("file") required() action { (x, c) => c.copy(file = x) } text ("file for test")
      opt[Int]("nFiles") required() action { (x, c) => c.copy(nFiles = x) } text ("count of files. corresponds to partitions")
      opt[Int]("fSize") required() action { (x, c) => c.copy(fSize = x) } text ("file size (in Bytes)")
      opt[String]("log") required() action { (x, c) => c.copy(log = x) } text ("file for output logs")
    }

    parser.parse(args, CliOptions()) match {
      case Some(config) => {
        var sparkConf = new SparkConf()
          .setAppName("Spark DfsIO Test")
          .set("spark.hadoop.dfs.replication", "1")

        val sc = new SparkContext(sparkConf)

        val fSizeBV: Broadcast[Int] = sc.broadcast(config.fSize)
        val fs = FileSystem.get(URI.create(config.log), new Configuration(true))

        config.mode match {
          case "write" => {
            // Create a Range and parallelize it, on nFiles partitions
            // The idea is to have a small RDD partitioned on a given number of workers
            // then each worker will generate data to write
            val a = sc.parallelize(1 until config.nFiles + 1, config.nFiles)

            val b = a.map(i => {
              // generate an array of Byte (8 bit), with dimension fSize
              // fill it up with "0" chars, and make it a string for it to be saved as text
              // TODO: this approach can still cause memory problems in the executor if the array is too big.
              val x = Array.ofDim[Byte](fSizeBV.value).map(x => "0").mkString("")
              x
            })

            // Force computation on the RDD
            sc.runJob(b, (iter: Iterator[_]) => {})

            // Write output file
            val (junk, timeW) = profile {
              b.saveAsTextFile(config.file)
            }

            // Write statistics
            fs.create(new Path(config.log), true)
              .writeUTF("\nTotal volume         : " + (config.nFiles.toLong * config.fSize) + " Bytes" +
                "\nTotal write time     : " + (timeW / 1000.toFloat) + " s" +
                "\nAggregate Throughput : " + (config.nFiles * config.fSize.toLong) / (timeW / 1000.toFloat) + " Bytes per second\n")
          }
          case "read" => {
            // Load file(s)
            val b = sc.textFile(config.file, config.nFiles)
            val (c, timeR) = profile {
              b.map(x => "0").take(1)
            }
            // BE CAREFUL! This is wrong: Spark is smart enough to read only one partition from which it gets the first element, so the read time is bogus
            // FIX ME :(

            // Write stats
            fs.create(new Path(config.log), true)
            .writeUTF("\nTotal volume      : " + (config.nFiles * config.fSize.toLong) + " Bytes"+
              "\nTotal read time   : " + (timeR / 1000.toFloat) + " s"+
              "\nAggregate Throughput : " + (config.nFiles * config.fSize.toLong) / (timeR / 1000.toFloat) + " Bytes per second\n")
          }
          case _ => println("Unknown command " + config.mode + ", expected one of write|read")
        }

        // Close open stat file and Spark Context
        fs.close()
        sc.stop()
      }

    }
  }

}
