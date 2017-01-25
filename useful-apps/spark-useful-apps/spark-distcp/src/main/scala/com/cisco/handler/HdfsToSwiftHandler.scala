package com.cisco.handler

import com.incu6us.openstack.swiftstorage.SwiftStorage
import org.apache.hadoop.fs.{FileSystem, Path}
import org.openstack4j.model.common.Payloads
import org.openstack4j.model.identity.Access

/**
 * Created by vpryimak on 04.11.2015.
 */
class HdfsToSwiftHandler(fs: FileSystem, path: Path, swiftStorage: SwiftStorage, container: String, `object`: String, filePath: String) extends Runnable {
  def run(): Unit = {

    println("Writing (container: "+container+") -> "+ `object`.replaceAll("^/|/$", "")+"/"+filePath)

    val inStream = fs.open(path)
    swiftStorage.writeToStorage(container, `object`.replaceAll("^/|/$", "")+"/"+filePath, Payloads.create(inStream))
    inStream.close()

  }
}
