package com.cisco.mantl.demo.streaming

/**
 * Command line options.
 */
case class Config(
                   zkQuorum: String = "127.0.0.1:2181",
                   group: String = "my-group",
                   topics: String = "test",
                   numThreads: Int = 1,
                   outDir: String = ""
                   )
