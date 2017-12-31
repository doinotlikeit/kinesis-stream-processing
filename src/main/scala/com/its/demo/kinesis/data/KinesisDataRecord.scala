package com.its.demo.kinesis.data

import scala.beans.BeanProperty

case class KinesisDataRecord() {
  @BeanProperty var recordId: String = null
  @BeanProperty var result: String = null
  @BeanProperty var data: String = null
}
