package com.cisco.mantl.cassandra

import com.datastax.spark.connector.cql.{ColumnDef, TableDef}
import com.datastax.spark.connector.rdd.{CassandraTableScanRDD}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkContext, SparkConf}
import com.datastax.spark.connector._
import java.net.URI

/**
 * Created by dbort on 27.10.2015.
 */
object CHDriver {

  def main(args: Array[String]) {

    Thread.sleep(5000) //turn on debug - $ export SPARK_JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

    val parser = new scopt.OptionParser[CLIConfig](MsgUsage) {
      head(NameApp, "1.0")
      opt[String]('h', "host") required() valueName ("<cassHost>") action { (x, c) =>
        c.copy(cassHost = x.trim)
      } validate { x => if (x.split(":").size == 2) success else failure("Value of --host must conform to format <hostname>:<port>")
      } text (MsgHost)
      opt[String]('t', "table") required() valueName ("<cassTableFQN>") action { (x, c) =>
        c.copy(cassTableFQN = x.trim)
      } validate { x => if (x.split("\\.").size == 2) success else failure("Value of --table must conform to format <keyspace>.<table>")
      } text (MsgTable)
      opt[String]('o', "out") required() valueName ("<outURI>") action { (x, c) =>
        c.copy(out = x.trim)
      } text (MsgOut)
      opt[Long]('b', "bsize") valueName ("<blockSize>") action { (x, c) =>
        c.copy(blockSize = x)
      } text (MsgHdfsBlockSize)
      opt[String]('m', "master") valueName ("<masterURI>") action { (x, c) =>
        c.copy(master = x.trim)
      } text (MsgMaster)
      opt[String]('n', "name") valueName ("<appName>") action { (x, c) =>
        c.copy(name = x.trim)
      } text (MsgName)
      help("help") text (MsgHelp)
      note(MsgNote)
    }

    parser.parse(args, CLIConfig()) match {
      case Some(cliConfig) =>

        val cassHost = cliConfig.cassHost.split(":")

        val sparkConfig = new SparkConf()
          .set("spark.cassandra.connection.host", cassHost(0))
          .set("spark.cassandra.connection.port", cassHost(1))
        if (!cliConfig.name.isEmpty) sparkConfig.setAppName(cliConfig.name)
        if (!cliConfig.master.isEmpty) sparkConfig.setMaster(cliConfig.master)


        val sc = new SparkContext(sparkConfig)
        sc.hadoopConfiguration.setBoolean("fs.hdfs.impl.disable.cache", true)

        val bSize = cliConfig.blockSize
        if (bSize > 0) sc.hadoopConfiguration.setLong("dfs.blocksize", bSize * Mb) //check by - example: hadoop fs -stat %o /user/examples1/files-out/part-00002

        sc.saveToHDFS(cliConfig.cassTableFQN, cliConfig.out)

        sc.stop()

      case None => println("ERROR: bad argument set provided")
    }
  }

  implicit class CassandraUtils(val origin: SparkContext) {

    def saveToHDFS(tableFQN: String, outputDir: String, report: Boolean = true): Unit = {

      val out = if (outputDir.endsWith("/")) outputDir else outputDir + "/"
      val tblFQN = tableFQN.split("\\.")

      val table = origin.cassandraTable(tblFQN(0), tblFQN(1))
      val tableDef = table.tableDef

      val partitionsColRefs = tableDef.partitionKey.map(colDef => colDef.ref)
      val partitionKeys = table.select(partitionsColRefs: _*).distinct().cache().collect()

      if (partitionsColRefs.length == 1) table.saveAsTextFile(out)
      else {
        partitionKeys.foreach {
          crPart =>
            val keys = crPart.columnNames
            val vals = crPart.columnValues
            val partData = table.where(getCondition(keys), vals: _*).map(cr => cr.dataAsString)
            partData.saveAsTextFile(out + vals.map(_.toString.replaceAll("[-+.^:,\\s+]", "")).mkString("-"))
        }
      }

      def getCondition(keys: IndexedSeq[String]): String = {
        keys.map(key => s"$key=?").mkString(" AND ")
      }

      if (report) writeInfo(tableDef, partitionKeys.length, out)

    }

    private def writeInfo(tableDef: TableDef, partNum: Int, outputDir: String): Unit = {
      lazy val fs = FileSystem.get(URI.create(outputDir), origin.hadoopConfiguration)
      try {
        fs.create(new Path(outputDir + "info.txt"), true)
          .writeUTF(
            "\nKeyspace : " + tableDef.keyspaceName + "\n" +
              "Table : " + tableDef.tableName + "\n" +
              "Partition key : " + tableDef.partitionKey.map(colDef => colDef.columnName).mkString(", ") + "\n" +
              "Clustering columns : " + tableDef.clusteringColumns.map(colDef => colDef.columnName).mkString(", ") + "\n" +
              "Regular columns : " + tableDef.regularColumns.map(colDef => colDef.columnName).mkString(", ") + "\n" +
              "CQL : " + tableDef.cql + "\n" +
              "Partitions processed : " + partNum + "\n"
          )
      } catch {
        case e@(_: java.io.IOException) => {
          println(getClass.getCanonicalName + " generated: " + e)
        }
      } finally {
        if (fs != null) fs.close()
      }
    }
  }

}

case class CLIConfig(cassHost: String = "",
                     cassTableFQN: String = "",
                     out: String = "",
                     blockSize: Long = 0,
                     master: String = "",
                     name: String = NameApp)


