package com.its.demo.kinesis.data

import com.amazonaws.services.lambda.runtime.{ClientContext, CognitoIdentity, Context, LambdaLogger}
import org.slf4j.LoggerFactory

/**
  * =TestContext=
  *
  * Used for JUnit testing.
  *
  * @author rajiv.cooray@itctcb.com
  * @since Dec 2017
  */
class TestContext extends Context {
  val logger = LoggerFactory.getLogger(this.getClass)

  override def getFunctionName: String = ???

  override def getRemainingTimeInMillis: Int = ???

  override def getLogger: LambdaLogger = new LambdaLogger {
    override def log(string: String): Unit = {
      logger.info(string)
    }
  }

  override def getFunctionVersion: String = ???

  override def getMemoryLimitInMB: Int = ???

  override def getClientContext: ClientContext = ???

  override def getLogStreamName: String = ???

  override def getInvokedFunctionArn: String = ???

  override def getIdentity: CognitoIdentity = ???

  override def getLogGroupName: String = ???

  override def getAwsRequestId: String = ???
}
