package com.cisco.mantl.hat

import com.cisco.mantl.hat.job.{PredictActivity, RecognizeActivity}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * Created by dbort on 24.11.2015.
 */
object HATDriver {

  def main(args: Array[String]) {

    Thread.sleep(5000) //turn on debug - $ export SPARK_JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

    val parser = new scopt.OptionParser[CLIConfig](MsgUsage) {
      head(NameApp, "1.0")
      opt[String]('h', "host") required() valueName "<cassHost>" action { (x, c) =>
        c.copy(cassHost = x.trim)
      } validate { x => if (x.split(":").size == 2) success else failure("Value of --host must conform to format <hostname>:<port>")
      } text MsgHost
      opt[String]('t', "table") required() valueName "<cassTableFQN>" action { (x, c) =>
        c.copy(cassTableFQN = x.trim)
      } validate { x => if (x.split("\\.").size == 2) success else failure("Value of --table must conform to format <keyspace>.<table>")
      } text MsgTable
      opt[String]('f', "function") required() valueName "<function>" action { (x, c) =>
        c.copy(function = x.trim)
      } validate { x => if (x == FuncRecognize || x == FuncPredict) success else failure(s"Value of --function must be in: $FuncRecognize, $FuncPredict")
      } text MsgFunction
      opt[Int]('u', "users") required() valueName "<users>" action { (x, c) =>
        c.copy(users = x)
      } validate { x => if (x > 0) success else failure("Value of --users must be > 0")
      } text MsgUsers
      opt[String]('m', "master") valueName "<masterURI>" action { (x, c) =>
        c.copy(master = x.trim)
      } text MsgMaster
      opt[String]('n', "name") valueName "<appName>" action { (x, c) =>
        c.copy(name = x.trim)
      } text MsgName
      help("help") text MsgHelp
      note(MsgNote)
    }

    parser.parse(args, CLIConfig()) match {
      case Some(cliConfig) =>

        val cassHost = cliConfig.cassHost.split(":")
        val tblFQN = cliConfig.cassTableFQN.split("\\.")

        val sparkConfig = new SparkConf()
          .set("spark.cassandra.connection.host", cassHost(0))
          .set("spark.cassandra.connection.port", cassHost(1))
        if (!cliConfig.name.isEmpty) sparkConfig.setAppName(cliConfig.name)
        if (!cliConfig.master.isEmpty) sparkConfig.setMaster(cliConfig.master)

        val sc = new SparkContext(sparkConfig)

        cliConfig.function match {
          case FuncRecognize => RecognizeActivity.apply(sc, tblFQN(0), tblFQN(1), cliConfig.users)
          case FuncPredict => PredictActivity.apply(sc)
          case _ => println("ERROR: not supported function")
        }

        sc.stop()

      case None => println("ERROR: bad argument set provided")
    }

  }

}

case class CLIConfig(cassHost: String = "",
                     cassTableFQN: String = "",
                     function: String = "",
                     users: Int = 0,
                     master: String = "",
                     name: String = NameApp)
