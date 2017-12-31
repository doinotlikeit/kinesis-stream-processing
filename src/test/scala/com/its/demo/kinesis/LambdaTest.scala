import java.util

import com.amazonaws.services.lambda.runtime.Context
import com.its.demo.kinesis.LambdaHandler
import com.its.demo.kinesis.data.{KinesisDataRecord, KinesisDataRecords, TestContext}
import org.junit.Assert._
import org.junit.{Before, Test}
import org.slf4j.LoggerFactory


class LambdaTest {

  val logger = LoggerFactory.getLogger(this.getClass)
  var handler: LambdaHandler = null

  @Before
  def initialize(): Unit = {
    handler = new LambdaHandler()
    logger.info("==> Initialised ...")
  }

  @Test
  def createHandlerTest() = {

    logger.info("==> Running test ...")
    assert(handler != null, "Handler instance is null")
  }

  @Test
  def checkTest() = {

    val inputMap: util.Map[String, Object] = new util.LinkedHashMap[String, Object]()
    inputMap.put("invocationId", "a4e9d125-8831-4840-8b13-5be1a41660c5")
    inputMap.put("deliveryStreamArn", "arn:aws:firehose:us-west-2:301286612886:deliverystream/its-demo-kinesis-firehose")
    inputMap.put("region", "us-west-2")

    val recordMap: util.Map[String, Object] = new util.LinkedHashMap[String, Object]()
    recordMap.put("recordId", "49580249382472375720749578939849415291010732558131396610000000")
    recordMap.put("approximateArrivalTimestamp", "1514652614490")
    recordMap.put("data", "MjAwOCwxLDMsNCwyMDAzLDE5NTUsMjIxMSwyMjI1LFdOLDMzNSxONzEyU1csMTI4LDE1MCwxMTYsLTE0LDgsSUFELFRQQSw4MTAsNCw4LDAsLDAsTkEsTkEsTkEsTkEsTkE=")
    val recordList: util.ArrayList[util.Map[String, Object]] = new util.ArrayList[util.Map[String, Object]]()
    recordList.add(recordMap)
    inputMap.put("records", recordList)
    logger.info(s"==> Sending test data: ${inputMap} to Lambda Handler")

    val ctx: Context = new TestContext()
    val result: KinesisDataRecords = handler.handleRequest(inputMap, ctx)
    assertNotNull("*** Result is null ***", result)

    assertNotNull("records attribute is null", result.getRecords)

    assertTrue("records is empty", !result.records.isEmpty())

    val records: java.util.List[KinesisDataRecord] = result.records
    records.forEach(rec => {
      assertNotNull("Record Id is null", rec.getRecordId)
      assertNotNull("Result is null", rec.getResult)
      assertNotNull("Record data Id is null", rec.getData)
      logger.info( s"==> Record id: ${rec.recordId}, Result: ${rec.result}, data: ${rec.data}")
    })

    logger.info("==> Test Done ...")
  }

}