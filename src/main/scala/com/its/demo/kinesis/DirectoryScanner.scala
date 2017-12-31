package com.its.demo.kinesis

import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}
import java.util.concurrent.{Executors, LinkedBlockingDeque, ScheduledExecutorService, TimeUnit}

import org.slf4j.LoggerFactory

/**
  * =DirectoryScanner=
  *
  * Scan a specified directory for CSV files, read all files found in the directory and place file contents in the
  * supplied queue.
  *
  * @author rajiv.cooray@itctcb.com
  * @since Dec 2017
  */

class DirectoryScanner(readQueue: LinkedBlockingDeque[String], directoryName: String, directoryScanInterval: Int) {

  val logger = LoggerFactory.getLogger(this.getClass)

  val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
  var isProcessingAFile = false
  var isRunning = true

  sys.addShutdownHook {
    scheduler.shutdown()
    logger.info("===> Shutting down ...")
    isRunning = false
  }

  // Scan the target directory at the specified frequency and process any files found
  scheduler.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = {

      try {

        // Stop trying to scan the directory if file processing is already happening
        if (isProcessingAFile == true) return

        logger.info(s"===> Thread [${Thread.currentThread().getName}], Scanning: $directoryName ...")
        val sourceDirectory = new File(directoryName)
        val listOfFiles = sourceDirectory.listFiles().filter(_.isFile).filter(_.getName.endsWith("csv"))
        logger.info(s"===> Found: ${listOfFiles.length} files in the source directory: [$directoryName], " +
          s"can read|execute: [${sourceDirectory.canRead}|${sourceDirectory.canExecute}]")
        listOfFiles.foreach((fileInDirectory: File) => {
          isProcessingAFile = true
          logger.info(s"===> Thread: [${Thread.currentThread().getName}], Processing file: [$fileInDirectory]")
          val reader: BufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileInDirectory)))
          val fileSize: Long = fileInDirectory.length()
          logger.info(s"===> Data file: [${fileInDirectory.getName}], size: ${(fileSize / 1024) / 1024} MB ...")
          var currentReadSz, totalReadSz, lineCount: Long = 0
          var oldPercent, newPercent: Long = 0
          var str: String = reader.readLine()

          while (str != null && isRunning == true) {
            currentReadSz = Int.int2long(str.length)
            totalReadSz += currentReadSz
            newPercent = (totalReadSz * 100) / fileSize
            if ((lineCount % 10000) == 0) {
              logger.info(s"Percentage complete: ${newPercent / 10}%,  Lines in queue: ${readQueue.size()}, lines read: ${lineCount} ")
            }
            oldPercent = newPercent
            lineCount += 1
            readQueue.put(str)
            str = reader.readLine()
          }

          logger.info(s"Percentage complete: ${newPercent / 10}%,  Lines read from file: $lineCount ")
          logger.info(s"===> Total lines in file: $lineCount lines")
          logger.info(s"===> File size: $fileSize, read size: $totalReadSz")
          logger.info("===> **********************************************************************************")
          logger.info(s"===> Thread: [${Thread.currentThread().getName}], Finished Processing file: [$fileInDirectory]")
          logger.info("===> **********************************************************************************")
          isProcessingAFile = false
        })

      } catch {
        case e: Throwable => {
          logger.error(s"Couldn't scan directory: [$directoryName] for new Zip files", e)
        }
      }
    }
  }, 0, directoryScanInterval, TimeUnit.MILLISECONDS)

}
