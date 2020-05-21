/*
 *  Copyright 2020 Expedia Group
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.expedia.www.haystack.attributor

import com.codahale.metrics.JmxReporter
import com.expedia.www.haystack.attributor.http.ManagedHttpService
import com.expedia.www.haystack.commons.kstreams.app.ManagedService
import com.expedia.www.haystack.commons.logger.LoggerUtils
import org.slf4j.LoggerFactory

import scala.util.Try

/**
  * Application lifecycle. Creation and destroy tasks
  *
  * @param stream      Kafka Stream
  * @param httpService http service
  * @param jmxReporter metrics jmxreporter
  */
class ManagedApplication(stream: ManagedService,
                         httpService: ManagedHttpService,
                         jmxReporter: JmxReporter) {

  private val LOGGER = LoggerFactory.getLogger(classOf[ManagedApplication])

  require(stream != null)
  require(jmxReporter != null)

  def start(): Unit = {
    try {
      jmxReporter.start()
      LOGGER.info("Starting the given topology and service")

      stream.start()
      LOGGER.info("kafka stream started successfully")

      httpService.start()
      LOGGER.info("http service started successfully")

    } catch {
      case ex: Exception =>
        LOGGER.error("Observed fatal exception while starting the app", ex)
        stop()
        System.exit(1)
    }
  }

  /**
    * This method stops the given `StreamsRunner` and `JmxReporter` is they have been
    * previously started. If not, this method does nothing
    */
  def stop(): Unit = {
    LOGGER.info("Shutting down kafka stream")
    Try(stream.stop())

    LOGGER.info("Shutting down http service")
    Try(httpService.close())

    LOGGER.info("Shutting down jmxReporter")
    Try(jmxReporter.close())

    LOGGER.info("Shutting down logger. Bye!")
    Try(LoggerUtils.shutdownLogger())
  }
}