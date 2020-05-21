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

package com.expedia.www.haystack.attribution.persistence.email

import com.codahale.metrics.JmxReporter
import com.expedia.www.haystack.attribution.commons.entities.TagsOperator
import com.expedia.www.haystack.attribution.persistence.email.config.AppConfiguration
import com.expedia.www.haystack.attribution.persistence.email.config.entities.{AwsEmailNotifyConfiguration, SmtpEmailNotifyConfiguration}
import com.expedia.www.haystack.attribution.persistence.email.notifier.{AwsEmailNotifier, SmtpEmailNotifier}
import com.expedia.www.haystack.commons.logger.LoggerUtils
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import org.slf4j.LoggerFactory

import scala.concurrent.duration.DAYS

/**
  * Starting point for application for sending attribution report as an email
  */
object App extends MetricsSupport {
  private val LOGGER = LoggerFactory.getLogger(App.getClass)
  private val TOTAL_SERVICE_NAME = "Total"

  def main(args: Array[String]): Unit = {
    val jmxReporter: JmxReporter = JmxReporter.forRegistry(metricRegistry).build()
    try {
      jmxReporter.start()
      val appConfiguration = new AppConfiguration()
      val currentTimeMs = System.currentTimeMillis()
      val previousDayStartTimeMs = currentTimeMs - (currentTimeMs % DAYS.toMillis(1)) - DAYS.toMillis(1)
      val previousDayEndTimeMs = currentTimeMs - (currentTimeMs % DAYS.toMillis(1))
      TagsOperator.apply(appConfiguration.tagsConfigList)

      val serviceStats = StatsFetcher.fetch(appConfiguration, previousDayStartTimeMs, previousDayEndTimeMs)
      val allServiceCombined = serviceStats.reduce(_ + _).copy(serviceName = TOTAL_SERVICE_NAME, lastSeen = null)

      val updatedServiceStats = serviceStats :+ allServiceCombined

      if (appConfiguration.emailNotifyConfig.`type` equalsIgnoreCase "email_aws") {
        val emailNotifier = new AwsEmailNotifier(appConfiguration.emailNotifyConfig.asInstanceOf[AwsEmailNotifyConfiguration], appConfiguration.environmentName, appConfiguration.emailTemplate)
        emailNotifier.send(updatedServiceStats)
      } else if (appConfiguration.emailNotifyConfig.`type` equals "email_smtp") {
        val emailNotifier = new SmtpEmailNotifier(appConfiguration.emailNotifyConfig.asInstanceOf[SmtpEmailNotifyConfiguration], appConfiguration.environmentName, appConfiguration.emailTemplate)
        emailNotifier.send(updatedServiceStats)
      }
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
