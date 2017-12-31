package com.its.demo.kinesis.data

import scala.beans.BeanProperty


class KinesisDataRecords {
  @BeanProperty var records: java.util.List[KinesisDataRecord] = null
}
