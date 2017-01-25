package com.cisco.mantl

/**
 * Command line options.
 */
case class CliOptions(
		mode:String = "fromHdfstoHdfs",
    swiftContainer: String = "sasa",
    swiftUri: String = "",
    hdfsUri: String = "/tmp/",
    hdfsSrc: String = "",
    hdfsDst: String = "",
    swiftEndpoint: String = "",
    swiftUsername: String = "",
    swiftPassword: String = "",
    swiftTenant: String = "",
    swiftConf: String = "/etc/swift.conf"
)
