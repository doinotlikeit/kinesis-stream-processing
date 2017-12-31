package com.its.demo.kinesis

import java.nio.charset.StandardCharsets
import java.util
import java.util.Base64

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.its.demo.kinesis.data.{EventData, EventDataHandler, EventType}

import scala.beans.BeanProperty
import scala.collection.mutable

/**
  * =LambdaHandler=
  *
  * Receive CSV data records from the Kinesis Firehose Endpoint, transform to POJOs and return in the expected Kinesis format.
  * The POJOs contain the CSV data converted to JSON.
  *
  * @author rajiv.cooray@itctcb.com
  * @since Dec 2017
  */

class LambdaHandler extends RequestHandler[Object, KinesisDataRecords] {
  var count: Int = 0

  /**
    * Process input data and return transformed data.
    * <p />
    *
    * @param input   - CSV data records
    * @param context - Kinesis Firehose context
    * @return KinesisDataRecords - transformed data
    */
  override def handleRequest(input: Object, context: Context): KinesisDataRecords = {

    var outRecordList: mutable.Buffer[KinesisDataRecord] = null
    var outRecordsList: KinesisDataRecords = null

    try {

      /**
        * Input data from Kinesis Firehose is in the following format:
        * {
        * "invocationId" : Lambda invocation ID (random GUID)
        * "applicationArn" : Kinesis Analytics application ARN,
        * "streamArn" : Source delivery stream ARN of the records,
        * "records": [
        * {
        * "recordId" : random GUID,
        * "kinesisFirehoseRecordMetadata" : {
        * "approximateArrivalTimestamp" : Approximate time that the delivery stream received the record,
        * },
        * "data" : base64 encoded user payload
        * }
        * ]
        * }
        */
      // Get the entire Input as a Map
      val inputMap: mutable.Map[String, Object] = collection.JavaConverters.mapAsScalaMap(input.asInstanceOf[util.LinkedHashMap[String, Object]])
      //context.getLogger.log("**** Got data map: " + inputMap)

      // Fetch the List of data records from the Input Map
      val recordList: mutable.Buffer[util.LinkedHashMap[Object, Object]] =
        collection.JavaConverters.asScalaBuffer(inputMap("records").asInstanceOf[util.ArrayList[util.LinkedHashMap[Object, Object]]])
      //context.getLogger.log("**** Got record list: " + recordList)

      // Transform each CSV record to a JSON representation
      outRecordList = recordList.map((recordItemMap: util.LinkedHashMap[Object, Object]) => {

        // context.getLogger.log("**** Got record map: " + recordMap)
        // Decode the CSV data string in the 'data' element
        val dataDecoded = new String(Base64.getDecoder().decode(recordItemMap.get("data").asInstanceOf[String]), "ASCII")
        // context.getLogger.log("**** Got data decoded: " + dataDecoded)

        // Transform and get a EventData instance, get the JSON string, encode it and set in the response to be sent back
        val eventData: EventData = EventDataHandler.process(dataDecoded, context.getLogger)
        var processingStatus = "Ok"
        if (eventData.eventType == EventType.ERROR_DATA_REC.toString) processingStatus = "ProcessingFailed"
        new KinesisDataRecord(recordItemMap.get("recordId").asInstanceOf[String], processingStatus,
          Base64.getEncoder.encodeToString(eventData.toJson().getBytes(StandardCharsets.UTF_8)))
      })

      /**
        * Create the response in the following format to be sent back to Kinesis runtime
        * {
        * "records": [
        * {
        * "recordId" : record ID that was passed,
        * "result"   : string with value - Ok, Dropped, ProcessingFailed
        * "data"     : processed base64-encoded user payload
        * }
        * ]
        * }
        */
      outRecordsList = new KinesisDataRecords(collection.JavaConverters.mutableSeqAsJavaList(outRecordList))
      // context.getLogger.log(s"*** Records to return: ${outRecordsList}")

      count += 1
      // context.getLogger.log(s"*** Handler call count: ${count}")

    } catch {
      case e: Throwable => {
        context.getLogger.log(s"Couldn't process the CSV data record received: ${e.getMessage}")
      }
    }

    outRecordsList
  }
}

case class KinesisDataRecord(@BeanProperty var recordId: String, @BeanProperty var result: String, @BeanProperty var data: String) {}

case class KinesisDataRecords(@BeanProperty var records: java.util.List[KinesisDataRecord]) {}


