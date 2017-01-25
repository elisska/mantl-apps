package com.cisco.dfsio.test

/**
 * Created by vpryimak on 04.11.2015.
 */
case class CliOptions(
                       mode: String = "read",
                       file: String = "",
                       nFiles: Int = 8,
                       fSize: Int = 20000000,
                       log: String = "testDfsIo.log"
                       )
