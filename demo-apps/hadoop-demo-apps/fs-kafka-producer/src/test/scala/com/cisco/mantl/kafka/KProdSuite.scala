package com.cisco.mantl.kafka

import java.io.{OutputStream, InputStreamReader, BufferedReader, File}
import java.net.InetAddress


import info.batey.kafka.unit.KafkaUnit
import kafka.producer.KeyedMessage
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import org.apache.hadoop.hdfs.MiniDFSCluster

/**
 * Created by dbort on 20.10.2015.
 */
class KProdSuite extends FunSuite with BeforeAndAfterAll {


  val payload1 = "SOMESTRING1"
  val payload2 = "SOMESTRING2"
  val delim = "Line-"
  val testDir = "/dir"
  val testInternalDir = testDir + "/" + "intdir"
  val topic = "testTopic"
  val currentHost = InetAddress.getLocalHost().getHostName()
  val zkHost = 5000
  val kafkaPort = 5001

  val kafkaUnitServer: KafkaUnit = new KafkaUnit(zkHost, kafkaPort)
  kafkaUnitServer.startup


  val baseDir = getClass.getResource("/hdfs").getPath

  val conf = new Configuration()
  conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir)
  val builder = new MiniDFSCluster.Builder(conf)
  val hdfsCluster: MiniDFSCluster = builder.build();
  val hdfsURI = "hdfs://" + getHostString(currentHost, hdfsCluster.getNameNodePort().toString) + "/"
  val fs: FileSystem = hdfsCluster.getFileSystem


  test("readAndForwardDataSuccessfully") {
    val path1 = new Path(testDir + "/file1.txt")
    val path2 = new Path(testInternalDir + "/file2.txt")
    fs.mkdirs(new Path(testDir))
    fs.mkdirs(new Path(testInternalDir))

    lazy val out1: OutputStream = fs.create(path1, true)
    lazy val out2: OutputStream = fs.create(path2, true)
    try {
      out1.write(payload1.getBytes())
      out2.write(payload2.getBytes())
    } finally {
      out1.close()
      out2.close()
    }

    //    val br = new BufferedReader(new InputStreamReader(fs.open(path)))
    //    val line: String = br.readLine()
    //    br.close()

    kafkaUnitServer.createTopic(topic)

    val testCore = new KProdCore(getHostString(currentHost, kafkaPort.toString), topic, new Path(hdfsURI + testDir), getHostString(currentHost, zkHost.toString), conf)
    testCore.run()

    Thread.sleep(10000) // wait until message delivered

    //kafkaUnitServer.sendMessages(new KeyedMessage[String, String]("testTopic", payload, payload))

    val messages: java.util.List[String] = kafkaUnitServer.readMessages(topic, 2)

    assert(messages.get(0).substring(messages.get(0).lastIndexOf(delim)) == delim + payload1)
    assert(messages.get(1).substring(messages.get(1).lastIndexOf(delim)) == delim + payload2)

  }

  override def afterAll() {
    fs.close()
    hdfsCluster.shutdown()
    kafkaUnitServer.shutdown()
  }

  private def getHostString(hostName: String, port: String): String = {
    hostName + ":" + port
  }

}
