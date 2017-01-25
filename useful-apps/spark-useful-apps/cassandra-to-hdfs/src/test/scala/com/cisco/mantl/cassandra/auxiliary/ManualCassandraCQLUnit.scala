package com.cisco.mantl.cassandra.auxiliary

import org.cassandraunit.CassandraCQLUnit
import org.cassandraunit.dataset.CQLDataSet

/**
 * Created by root on 11/13/15.
 */
class ManualCassandraCQLUnit(dataSet: CQLDataSet) extends CassandraCQLUnit(dataSet){
  load()
}
