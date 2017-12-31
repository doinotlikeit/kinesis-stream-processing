package com.its.demo.kinesis

import java.nio.ByteBuffer
import java.util.concurrent.{LinkedBlockingDeque, TimeUnit}

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.kinesisfirehose.model.{PutRecordRequest, Record}
import com.amazonaws.services.kinesisfirehose.{AmazonKinesisFirehoseAsync, AmazonKinesisFirehoseAsyncClientBuilder}
import org.slf4j.LoggerFactory


/**
  * =KinesisClientApp=
  *
  * AWS Kinesis Firehose Client Application.
  *
  * @author rajiv.cooray@itctcb.com
  * @since Dec 2017
  */

object KinesisClientApp extends App {

  val DIRECTORY_SCAN_INTERVAL = 300000
  val DELIVERY_STREAM_NAME = "its-demo-kinesis-firehose"
  val logger = LoggerFactory.getLogger(this.getClass)

  // Uncomment for Async
  var firehoseClient: AmazonKinesisFirehoseAsync = null

  // Uncomment for Sync
  //var firehoseClient: AmazonKinesisFirehose = null

  var dataDirName: String = null
  val dataRecordsQueue = new LinkedBlockingDeque[String](75000)
  var isRunning = true

  if (!(args.length > 0)) throw new IllegalStateException("Please specify the directory to scan for data files")
  dataDirName = args(0)

  try {

    val firehoseClientBulder = AmazonKinesisFirehoseAsyncClientBuilder.standard()
      .withCredentials(new ProfileCredentialsProvider())
      .withRegion(Regions.US_WEST_2.getName)

    firehoseClient = firehoseClientBulder.build()

    logger.info(s"==> Created the Firehose client, data directory to scan: [${dataDirName}] ... ")

  } catch {
    case e: Throwable => {
      throw new IllegalStateException("Couldn't create the Kinesis client", e)
    }
  }

  sys.addShutdownHook {
    logger.info("===> Shutting down ...")
    isRunning = false
  }

  new DirectoryScanner(dataRecordsQueue, dataDirName, DIRECTORY_SCAN_INTERVAL)

  while (isRunning == true) {
    while (dataRecordsQueue.isEmpty()) {
      TimeUnit.SECONDS.sleep(1)
    }
    try {

      val dataLine: String = dataRecordsQueue.poll()
      val dataRecord = new Record()
      dataRecord.setData(ByteBuffer.wrap(dataLine.getBytes))
      //dataRecord.setData(buf)
      val putRecordRequest = new PutRecordRequest()
        .withDeliveryStreamName(DELIVERY_STREAM_NAME)
      putRecordRequest.setRecord(dataRecord)


      // Uncomment for Sync
      //      firehoseClient.putRecordAsync(putRecordRequest)
      //      val result: Future[PutRecordResult] = firehoseClient.putRecordAsync(putRecordRequest)
      //      logger.info(s"===> Put record (Sync) to Kinesis: ${result.get().getRecordId}, ${result.isDone} ...")


      // Uncomment for Async
      val recordRsp = firehoseClient.putRecord(putRecordRequest)
      logger.info(s"===> Put record (Async) to Kinesis: ${recordRsp.getRecordId} ...")


      // Uncomment for Batch
      //      import com.amazonaws.services.kinesisfirehose.model.PutRecordBatchRequest
      //      val putRecordBatchRequest = new PutRecordBatchRequest
      //      putRecordBatchRequest.setDeliveryStreamName(DELIVERY_STREAM_NAME)
      //      val sourceRecList: ListBuffer[Record] = new ListBuffer()
      //      sourceRecList += dataRecord
      //      putRecordBatchRequest.setRecords(collection.JavaConverters.asJavaCollection(sourceRecList))
      //      firehoseClient.putRecordBatch(putRecordBatchRequest)
      //      sourceRecList.clear()
      //      logger.info("=== Put record batch to Kinesis")

    } catch {
      case e: Throwable => {
        throw new IllegalStateException("Couldn't PUT data to Kinesis via client", e)
      }
    }
  }

  if (isRunning == false) sys.exit()

}