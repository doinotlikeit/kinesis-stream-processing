package com.its.demo.kinesis

import java.nio.charset.StandardCharsets
import java.util
import java.util.Base64

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.its.demo.kinesis.data.{EventData, EventDataHandler, KinesisDataRecord, KinesisDataRecords}

import scala.collection.mutable


class LambdaHandler extends RequestHandler[Object, KinesisDataRecords] {
  var count: Int = 0

  override def handleRequest(input: Object, context: Context): KinesisDataRecords = {

    var outRecordList: mutable.Buffer[KinesisDataRecord] = null
    var outRecordsList: KinesisDataRecords = null

    try {

      val inputMap: mutable.Map[String, Object] = collection.JavaConverters.mapAsScalaMap(input.asInstanceOf[util.LinkedHashMap[String, Object]])
      context.getLogger.log("**** Got data map: " + inputMap)

      val recordList: mutable.Buffer[util.LinkedHashMap[Object, Object]] =
        collection.JavaConverters.asScalaBuffer(inputMap("records").asInstanceOf[util.ArrayList[util.LinkedHashMap[Object, Object]]])
      context.getLogger.log("**** Got record list: " + recordList)

      outRecordList = recordList.map((recordItemMap: util.LinkedHashMap[Object, Object]) => {
        // context.getLogger.log("**** Got record map: " + recordMap)
        val dataDecoded = new String(Base64.getDecoder().decode(recordItemMap.get("data").asInstanceOf[String]), "ASCII")
        // context.getLogger.log("**** Got data decoded: " + dataDecoded)
        val eventData: EventData = EventDataHandler.process(dataDecoded, context.getLogger)
        //        new KinesisDataRecord(recordItemMap.get("recordId").asInstanceOf[String], "Ok",
        //          Base64.getEncoder.encodeToString(eventData.toJson().getBytes(StandardCharsets.UTF_8)))
        var rec = new KinesisDataRecord()
        rec.recordId = recordItemMap.get("recordId").asInstanceOf[String]
        rec.result = "Ok"
        rec.data = Base64.getEncoder.encodeToString(eventData.toJson().getBytes(StandardCharsets.UTF_8))
        rec
      })

      //outRecordsList = new KinesisDataRecords(collection.JavaConverters.mutableSeqAsJavaList(outRecordList))
      outRecordsList = new KinesisDataRecords()
      outRecordsList.records = collection.JavaConverters.mutableSeqAsJavaList(outRecordList)

      context.getLogger.log(s"*** Records to return: ${outRecordsList}")
      count += 1
      context.getLogger.log(s"*** Handler call count: ${count}")

    } catch {
      case e: Throwable => {
        context.getLogger.log(s"Couldn't handle: ${e.getMessage}")
      }
    }

    outRecordsList
  }

}
