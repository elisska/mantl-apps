package com.cisco.mantl.kafka

import java.io.{InputStreamReader, BufferedReader, FileNotFoundException}
import java.util.Properties
import java.util.concurrent.Executors


import kafka.admin.AdminUtils
import kafka.api.TopicMetadata
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import kafka.utils.ZKStringSerializer
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.serialize.ZkSerializer
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}

import scala.concurrent.ExecutionContext

/**
 * Created by dbort on 21.10.2015.
 */
class KProdCore(brokers: String, topic: String, inputDir: Path, zkHost: String, hConfig: Configuration) {

  def run(): Unit = {

    def getMetadata(zkhost: String, topic: String): TopicMetadata = {
      val zkClient = new ZkClient(zkhost, 10000, 10000);
      zkClient.setZkSerializer(getZkSerializer)
      AdminUtils.fetchTopicMetadataFromZk(topic, zkClient);
    }

    def validatePartitionNumber(partitionsNumber: Int): Int = {
      if (partitionsNumber > 0) {
        println(String.format("%s : %s partitions found for %s topic. Setting appropriate file reading pool size.",
          getClass.getCanonicalName, partitionsNumber.toString, topic))
        partitionsNumber
      } else throw new RuntimeException(String.format("Topic %s not found registered on Zookeeper host %s .", topic, zkHost))
    }

    implicit val ec = new ExecutionContext {
      val threadPool = Executors.newFixedThreadPool(validatePartitionNumber(getMetadata(zkHost, topic).partitionsMetadata.size));

      override def execute(runnable: Runnable): Unit = threadPool.submit(runnable);

      override def reportFailure(cause: Throwable): Unit = {
        println("Forwarding thread error: " + cause)
      };

      def shutdown() = threadPool.shutdown();
    }

    //start running
    val producerConfig = getProducerConfig(brokers)
    try {
      val fs = FileSystem.get(if (hConfig == null) new Configuration else hConfig)
      scanDirectoryTree(fs, inputDir)
    } catch {
      case e@(_: FileNotFoundException | _: java.io.IOException) => {
        println("File System Error: " + e)
      }
    }

    def scanDirectoryTree(fs: FileSystem, rootDir: Path): Unit = {
      val statuses = fs.listStatus(rootDir)
      if (statuses != null && statuses.length > 0)
        statuses.foreach(status =>
          if (status.isDirectory) scanDirectoryTree(fs, status.getPath)
          else ec.execute(readAndForward(fs, status.getPath))
        )
    }

    ec.shutdown()

    def readAndForward(fs: FileSystem, path: Path): Unit = {
      val producer = new Producer[String, String](producerConfig)
      lazy val br = new BufferedReader(new InputStreamReader(fs.open(path)))
      try {
        var line = br.readLine()
        while (line != null) {
          //System.out.println(line)
          producer.send(new KeyedMessage[String, String](topic, line.hashCode.toString, appendPrefix(line)))
          line = br.readLine()
        }
      } finally {
        br.close()
        producer.close();
      }
    }
  }

  private def getProducerConfig(brokers: String): ProducerConfig = {
    val props = new Properties()
    props.load(getClass.getResourceAsStream("/producer-defaults.properties"))
    props.put("metadata.broker.list", brokers)
    new ProducerConfig(props)
  }

  private def getZkSerializer(): ZkSerializer = new ZkSerializer {
    override def serialize(data: scala.Any): Array[Byte] = ZKStringSerializer.serialize(data.asInstanceOf[java.lang.Object])

    override def deserialize(bytes: Array[Byte]): AnyRef = ZKStringSerializer.deserialize(bytes)
  }

  private def appendPrefix(line: String): String = {
    String.format("Thread-%s;Time-%s;Line-%s", Thread.currentThread().getId.toString, System.nanoTime().toString, line)
  }

}
