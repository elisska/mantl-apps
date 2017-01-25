package com.cisco.mantl.integration

import com.cisco.mantl.integration.ItgDriver._
import com.github.tlrx.elasticsearch.test.EsSetup
import com.github.tlrx.elasticsearch.test.EsSetup._
import kafka.producer.KeyedMessage
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.elasticsearch.index.query.{FilterBuilders, QueryBuilders}
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
 * Created by dbort on 09.10.2015.
 */
class ItgSuite extends FunSuite with BeforeAndAfterAll {

  val kafkaUnitServer: KafkaUnit = new KafkaUnit(5000, 5001)
  kafkaUnitServer.startup

  val sparkConf = new SparkConf().setAppName("testContext").setMaster("local[4]")
  val ssc = new StreamingContext(sparkConf, Seconds(1)) //run stream every 1 second

  val esHost = "localhost"
  val esPort = "9200"
  val esIndex = "someindex"
  val esType = "sometype"
  val initialJson = """{"field111" : "value111" }"""

  val esSetup = new EsSetup();

  test("consumeAndPutJsonDataSuccessfully") {

    val testTopic = "TestTopic"
    kafkaUnitServer.createTopic(testTopic)
    val keyedMessage: KeyedMessage[String, String] = new KeyedMessage[String, String](testTopic, initialJson, initialJson)

    kafkaUnitServer.sendMessages(keyedMessage)

    ssc.putJson(ssc.consume(kafkaUnitServer.getBrokerString, testTopic), esHost, esPort, esIndex, esType)

    esSetup.execute(deleteAll()) //clean Elasticsearch indexes before starting Spark job

    ssc.start()
    //ssc.awaitTermination()
    Thread.sleep(10000) //wait 10 seconds before starting querying index

    val esClient = esSetup.client()
    val response = esClient.prepareSearch(esIndex)
      .setQuery(QueryBuilders.matchAllQuery())
      .setPostFilter(FilterBuilders.termFilter("field111", "value111"))
      .execute()
      .actionGet();

    assert(response.getHits().totalHits() == 1);
    println("INDEX EXISTS: " + esSetup.exists("someindex"))

    ssc.stop(true, true)

  }

  override def afterAll() {
    kafkaUnitServer.shutdown
    esSetup.terminate
  }

}
