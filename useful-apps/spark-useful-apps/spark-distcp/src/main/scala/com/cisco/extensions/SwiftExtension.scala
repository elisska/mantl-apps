package com.cisco.extensions

import java.net.URI
import java.util
import java.util.{concurrent, Properties}
import java.util.concurrent.{ThreadFactory, Executors, ExecutorService}

import com.cisco.handler.{HdfsToSwiftHandler, SwiftToHdfsHandler}
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.incu6us.openstack.swiftstorage
import com.incu6us.openstack.swiftstorage.SwiftStorage
import io.netty.util.internal.chmv8.ForkJoinPool.ForkJoinWorkerThreadFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import org.openstack4j.model.common.Payloads
import org.openstack4j.model.identity.Access
import org.openstack4j.model.storage.`object`.SwiftObject
import org.slf4j.{LoggerFactory, Logger}
//import sun.org.mozilla.javascript.internal.Callable
import scala.collection.JavaConversions._
import scala.concurrent.forkjoin.ForkJoinPool

class SwiftExtension(properties: Properties) {

  private lazy val LOGGER = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

  private val configuration = new Configuration(true)
//  private var properties: Properties = null

  /**
   * Set "Prporties":
   * swift.debug=false
   * swift.endpoint=https://exaple.com:5000/v2.0
   * swift.username=admin
   * swift.password=p@ssw0rd
   * swift.tenant=TENANT01
   */
//  def setProperties(properties: Properties): Unit = {
//    this.properties = properties
//  }

  /**
   * Converting Swift object to file on hdfs
   *
   * @param container
   * @param `object`
   * @param hdfsFile
   */
  def fromSwiftToHdfs(container: String, `object`: String, hdfsFile: String): Unit = {

    var swiftStorage: SwiftStorage = null

    if (properties != null) {
      swiftStorage = new SwiftStorage(properties)
    } else {
      swiftStorage = new SwiftStorage("swift", "swift.properties")
    }

    LOGGER.debug("Loading endpoint")
    LOGGER.debug("endpoint -> " + swiftStorage.getUtil.getProperties.getProperty("swift.endpoint"))

    val fs = FileSystem.get(URI.create(hdfsFile), configuration)
    val inStream = swiftStorage.readFromStorage(container, `object`.replaceFirst("^/", ""))
    val outStream: FSDataOutputStream = fs.create(new Path(hdfsFile))

    Iterator.continually(inStream.read)
      .takeWhile(-1 !=)
      .foreach(outStream.write(_))

    outStream.close()
    inStream.close()

  }

  /**
   * Write objects from Swift Storage to HDFS recursively
   * @param container
   * @param `object`
   * @param hdfsDir
   */
  def fromSwiftToHdfsWithRecursion(container: String, `object`: String, hdfsDir: String): Unit = {
    var swiftStorage: SwiftStorage = null

    if (properties != null) {
      swiftStorage = new SwiftStorage(properties)
    } else {
      swiftStorage = new SwiftStorage("swift", "swift.properties")
    }

    LOGGER.debug("Loading endpoint")
    LOGGER.debug("endpoint -> " + swiftStorage.getUtil.getProperties.getProperty("swift.endpoint"))

    val access: Access = swiftStorage.getClient.getAccess

    val objects = swiftStorage.listObjectsByPathFromContainer(container, `object`)

    objects.foreach(o => {
      LOGGER.debug("Copying -> " + o.getName)
      println("Copying -> " + o.getName)
      new SwiftToHdfsHandler(configuration, access, o, container, hdfsDir).run()
    })
  }

  /**
   * Convert HDFS file to Swift object
   *
   * @param container
   * @param `object`
   * @param hdfsFile
   */
  def fromHdfsToSwift(hdfsFile: String, container: String, `object`: String): Unit = {
    var swiftStorage: SwiftStorage = null

    if (properties != null) {
      swiftStorage = new SwiftStorage(properties)
    } else {
      swiftStorage = new SwiftStorage("swift", "swift.properties")
    }

    LOGGER.debug("endpoint -> " + swiftStorage.getUtil.getProperties.getProperty("swift.endpoint"))

    val fs = FileSystem.get(configuration)

    val inStream = fs.open(new Path(hdfsFile))

    swiftStorage.writeToStorage(container, `object`.replaceFirst("^/", ""), Payloads.create(inStream))

    inStream.close()
  }

  /**
   * Convert HDFS file to Swift object (with recursion on HDFS)
   * @param hdfsFile
   * @param container
   * @param `object`
   */
  def fromHdfsToSwiftWithRecursion(hdfsFile: String, container: String, `object`: String): Unit = {
    var swiftStorage: SwiftStorage = null

    if (properties != null) {
      swiftStorage = new SwiftStorage(properties)
    } else {
      swiftStorage = new SwiftStorage("swift", "swift.properties")
    }

    val fs = FileSystem.get(configuration)

    // file copy
    if (fs.getFileStatus(new Path(hdfsFile)).isFile) {
      val inStream = fs.open(new Path(hdfsFile))

      swiftStorage.writeToStorage(container, `object`.replaceFirst("^/", ""), Payloads.create(inStream))

      inStream.close()
    }
    // directory copy
    else {
      val tmpFs = fs.listFiles(new Path(hdfsFile), true)
      while (tmpFs.hasNext) {
        val filePath = tmpFs.next().getPath
        val fullFilePath = filePath.toUri.getPath.replaceAll("(^/"+hdfsFile.replace("/", "")+")/(.*)", "$2")
        new HdfsToSwiftHandler(fs, filePath, swiftStorage, container, `object`, fullFilePath).run()
      }
    }
  }

  /**
   * Copy files on HDFS
   *
   * @param src
   * @param dst
   */
  def copyOnHdfs(src: String, dst: String): Unit = {
    val configuration = new Configuration(true)

    val fsSrc = FileSystem.get(new URI(src), configuration)
    val fsDst = FileSystem.get(new URI(dst), configuration)
    FileUtil.copy(fsSrc, new Path(src), fsDst, new Path(dst), false, configuration)
  }
}