package com.cisco.mantl.cassandra

import java.net.InetAddress

import com.cisco.mantl.cassandra.auxiliary.ManualCassandraCQLUnit
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}

import org.apache.hadoop.hdfs.MiniDFSCluster
import org.apache.spark.{SparkContext, SparkConf}
import org.cassandraunit.{CassandraCQLUnit, DataLoader}
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import com.cisco.mantl.cassandra.CHDriver._

/**
 * Created by dbort on 27.10.2015.
 */
class CHSuite extends FunSuite with BeforeAndAfterAll {

  val currentHost = InetAddress.getLocalHost().getHostName()
  val baseDir = getClass.getResource("/hdfs").getPath

  val testDirSimple = "simpledir"
  val testDirPartitioned = "partitioneddir"

  val kayspace = "testkeyspace"
  val simpleTable = "simpletable"
  val partitionedTable = "partitionedtable"

  //start HDFS
  val conf = new Configuration()
  conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir)
  val hdfsCluster: MiniDFSCluster = new MiniDFSCluster.Builder(conf).build()
  val hdfsURI = "hdfs://" + getHostString(currentHost, hdfsCluster.getNameNodePort().toString) + "/"
  val fs: FileSystem = hdfsCluster.getFileSystem

  //start Cassandra
  EmbeddedCassandraServerHelper.startEmbeddedCassandra("another-cassandra.yaml", 300000)
  EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()

  //setup Spark
  val sparkConfig = new SparkConf()
    .set("spark.cassandra.connection.host", EmbeddedCassandraServerHelper.getHost)
    .set("spark.cassandra.connection.port", EmbeddedCassandraServerHelper.getNativeTransportPort.toString)
    .setAppName(NameApp)
    .setMaster("local")
  val sc = new SparkContext(sparkConfig)


  test("readSimpleTableDataAndSaveToHDFSSuccessfully") {

    val cassandraSimpleCQLUnit = new ManualCassandraCQLUnit(new ClassPathCQLDataSet("simple.cql", kayspace));
    sc.saveToHDFS(kayspace + "." + simpleTable, hdfsURI + testDirSimple)
    val arr = sc.textFile(hdfsURI + testDirSimple + "/part-00000").collect()
    val arrInfo = sc.textFile(hdfsURI + testDirSimple + "/info.txt").collect()

    assert(arr.size == 2)
    assert(arr(0) == "{id: key01, value: value01}")
    assert(arrInfo.size > 0)

  }


  test("readPartitionedTableDataAndSaveToHDFSSuccessfully") {

    val cassandraPartitionedCQLUnit = new ManualCassandraCQLUnit(new ClassPathCQLDataSet("partitioned.cql", kayspace));
    sc.saveToHDFS(kayspace + "." + partitionedTable, hdfsURI + testDirPartitioned)
    val arr1 = sc.textFile(hdfsURI + testDirPartitioned + "/id01-name01" + "/part-00000").collect()
    val arr2 = sc.textFile(hdfsURI + testDirPartitioned + "/id01-name02" + "/part-00000").collect()

    assert(arr1.size == 2)
    assert(arr1(0) == "{id: id01, name: name01, clkey: clkey0111, data: data0111}")
    assert(arr2.size == 1)
    assert(arr2(0) == "{id: id01, name: name02, clkey: clkey012, data: data012}")

  }


  override def afterAll() {
    if (sc != null) sc.stop()
    if (fs != null) fs.close()
    if (hdfsCluster != null) hdfsCluster.shutdown(true)
    EmbeddedCassandraServerHelper.stopEmbeddedCassandra()
    println("finished")
  }

  private def getHostString(hostName: String, port: String): String = {
    hostName + ":" + port
  }


}
