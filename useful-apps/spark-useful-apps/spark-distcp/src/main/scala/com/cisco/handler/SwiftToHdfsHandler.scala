package com.cisco.handler

import java.net.URI
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path, FSDataOutputStream}
import org.openstack4j.model.common.DLPayload
import org.openstack4j.model.identity.Access
import org.openstack4j.model.storage.`object`.SwiftObject

/**
 * Created by vpryimak on 03.11.2015.
 */
class SwiftToHdfsHandler(configuration: Configuration, access: Access, swiftObject: SwiftObject, container: String, hdfsDir: String) extends Runnable {

  def run(): Unit = {

    val dir: Path = new Path(hdfsDir + "/" + swiftObject.getName.replaceAll("(.*)/(.*)", "$1"))
    val dstFile: Path = new Path(hdfsDir + "/" + swiftObject.getName.replaceAll("(.*)/(.*)", "$1") + "/" + swiftObject.getName.replaceAll("(.*)/(.*)", "$2"))

    val fs = FileSystem.get(URI.create(hdfsDir + "/" + swiftObject.getName.replaceAll("(.*)/(.*)", "$1") + "/" + swiftObject.getName.replaceAll("(.*)/(.*)", "$2")), configuration)

    if (!fs.exists(dir)) {
      fs.mkdirs(dir)
    }

    var payload: DLPayload = swiftObject.download

    // Convert to InputStream
    val inStream = payload.getInputStream
    // Write to HDFS
    val outStream: FSDataOutputStream = fs.create(dstFile)

    Iterator.continually(inStream.read)
      .takeWhile(-1 !=)
      .foreach(outStream.write(_))

    Thread.sleep(500)

    outStream.close()
    inStream.close()
  }

}
