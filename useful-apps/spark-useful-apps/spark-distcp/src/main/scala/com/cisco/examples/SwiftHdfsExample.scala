package com.cisco.examples

import com.cisco.extensions.SwiftExtension
import com.incu6us.openstack.swiftstorage.SwiftStorage
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import scala.collection.JavaConversions._

/**
 * Created by vpryimak on 19.10.2015.
 */
object SwiftHdfsExample extends App {

  override def main(args: Array[String]) {

    //    val ss = new SwiftStorage("swift", "swift.properties")

    // list all containers
    //    ss.listAllContainers().foreach(item => println(item.getName))
    //    ss.listObjectsFromContainer("sasa").foreach(item => println(item))

    //    ss.listObjectsFromContainerLike("sasa", "20150224/tools.cisco.com/ccxrp-prod2-03/81/parsed_access_20150224.log").foreach(item => println(item.getName))

    // copy one file
    //    ss.copyToLocalFs("sasa", "20150224/tools.cisco.com/ccxrp-prod2-03/81/parsed_access_20150224_1.log", "d:\\tmp")

    // copy dir
    //    ss.copyToLocalFs("sasa", "20150224/tools.cisco.com/ccxrp-prod2-03/81", "d:\\tmp")

    // copy from swift to swift
    //    ss.copyToStorage("sasa", "20150224/tools.cisco.com/ccxrp-prod2-03/81/parsed_access_20150224.log", "20150224/tools.cisco.com/ccxrp-prod2-03/81/parsed_access_20150224_1.log")
    //    ss.listObjectsFromContainerLike("sasa", "20150224/tools.cisco.com/ccxrp-prod2-03/81/parsed_access_20150224_1.log").foreach(item => println(item.getName))

    // delete object
    //    ss.deleteObjectFromStorage("sasa", "20150224/tools.cisco.com/ccxrp-prod2-03/81/parsed_access_20150224_1.log")

    // write to storage
    //    ss.writeToStorage("sasa", "20150224/tmp.txt", Payloads.create(new File("D:\\tmp\\parsed_access_20150224_1.log")))
    //    ss.listObjectsFromContainerLike("sasa", "20150224/tmp.txt").foreach(item => println(item.getName))

    // *****
    /*
     * Write from Swift to HDFS
     */
    //        val ss = new SwiftStorage("com/cisco/sasa/swift.config", "swift.properties")
    //        val file = new File("D:\\tmp\\parsed_access_20150224_1.log")
    //    val hdfsFile = "hdfs://10.61.72.126:8020/tmp/"+file.getName
    //
    //    val configuration = new Configuration(true)
    //    configuration.set("fs.defaultFS","hdfs://10.61.72.126:8020")
    //    configuration.set("hadoop.home.dir","/opt/hadoop/hadoop-2.6.1")
    //
    //        val fs = FileSystem.get(URI.create(hdfsFile), configuration)
    ////    val inStream = new FileInputStream(file)
    //    val inStream = ss.readFromStorage("sasa", "20150224/tools.cisco.com/ccxrp-prod2-03/81/parsed_access_20150224.log")
    //    var outStream: FSDataOutputStream = null
    //
    //    outStream = fs.create(new Path(hdfsFile))
    //
    //    Iterator.continually(inStream.read)
    //      .takeWhile(-1 !=)
    //      .foreach(outStream.write(_))
    //
    //    outStream.close()
    //    inStream.close()

    // *****
    /*
     * Write from HDFS to Swift
     */
    //    val ss = new SwiftStorage("com/cisco/sasa/swift.config", "swift.properties")
    //
    //    val configuration = new Configuration(true)
    //    configuration.set("fs.defaultFS","hdfs://10.61.72.126:8020")
    //    configuration.set("hadoop.home.dir","/opt/hadoop/hadoop-2.6.1")
    //
    //    val hdfsFile = "hdfs://10.61.72.126:8020/tmp/parsed_access_20150224_1.log"
    //
    //    val fs = FileSystem.get(configuration)
    //    val inStream = fs.open(new Path(hdfsFile))
    //
    //    ss.writeToStorage("sasa", "tmp/tmp.txt", Payloads.create(inStream))
    //
    //    inStream.close()


    // *****
    /*
     * Write from HDFS to HDFS
     */
    //        val configuration = new Configuration(true)
    //        configuration.set("fs.defaultFS", "hdfs://10.61.72.126:8020")
    //        configuration.set("hadoop.home.dir", "/opt/hadoop/hadoop-2.6.1")
    //
    //        val fs = FileSystem.get(configuration)
    //    FileUtil.copy(fs, new Path("/tmp/parsed_access_20150224_1.log"),
    //      fs, new Path("/tmp1/"),
    //      false, configuration)

    // *****
    //    val configuration = new Configuration(true)
    //    configuration.set("fs.defaultFS", "hdfs://10.61.72.126:8020")
    //    configuration.set("hadoop.home.dir", "/opt/hadoop/hadoop-2.6.1")
    //
    //    val fs = FileSystem.get(configuration)
    //    println(fs.listFiles(new Path("hdfs://10.61.72.126:8020/"), true))

    // *****
    /*
     * Get objects starts with ...
     */
    //    ss.listObjectsFromContainerStartsWith("sasa", "20150224/").foreach(item => println(item.getName))

  }
}
