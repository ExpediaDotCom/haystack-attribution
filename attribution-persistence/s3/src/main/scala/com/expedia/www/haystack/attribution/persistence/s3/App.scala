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

package com.expedia.www.haystack.attribution.persistence.s3

import com.codahale.metrics.JmxReporter
import com.expedia.www.haystack.attribution.persistence.s3.config.AppConfiguration
import com.expedia.www.haystack.attribution.persistence.s3.persist.{S3FileLocation, S3Writer}
import com.expedia.www.haystack.commons.logger.LoggerUtils
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import org.slf4j.LoggerFactory

import scala.concurrent.duration.{DAYS, SECONDS}

/**
  * Starting point for application
  */
object App extends MetricsSupport {
  private val LOGGER = LoggerFactory.getLogger(App.getClass)
  private val currentTimeMs = System.currentTimeMillis()

  def main(args: Array[String]): Unit = {
    val jmxReporter: JmxReporter = JmxReporter.forRegistry(metricRegistry).build()

    val previousDayStartTimeMs = currentTimeMs - (currentTimeMs % DAYS.toMillis(1)) - DAYS.toMillis(1)
    val previousDayEndTimeMs = currentTimeMs - (currentTimeMs % DAYS.toMillis(1)) - SECONDS.toMillis(1)

    try {
      jmxReporter.start()
      val appConfiguration = new AppConfiguration()

      val serviceStats = StatsFetcher.fetch(appConfiguration, previousDayStartTimeMs, previousDayEndTimeMs)

      appConfiguration.persistS3Configs.items.foreach(s3Config => {
        if (s3Config.enabled) {
          s3Config.transformerId.foreach(transformerId => {
            val transformer = appConfiguration.transformers.filter(_.getId.equalsIgnoreCase(transformerId)).head

            val s3FileMetadata = S3FileLocation.getS3FileMetadata(s3Config.bucket, s3Config.folderPath, previousDayStartTimeMs)
            val csvString = transformer.transform(serviceStats)
            val s3Writer = new S3Writer(s3Config.bucket, s3Config.useStsRole, s3Config.stsRoleArn)
            s3Writer.send(csvString, s3FileMetadata.filePath)
          })
        }
      })
    } catch {
      case ex: Exception =>
        LOGGER.error("Observed fatal exception", ex)
        LoggerUtils.shutdownLogger()
        System.exit(1)
    } finally {
      jmxReporter.close()
    }
  }
}