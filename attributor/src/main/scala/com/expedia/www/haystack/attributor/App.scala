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
import com.expedia.www.haystack.attribution.commons.entities.TagsOperator
import com.expedia.www.haystack.attributor.config.AppConfiguration
import com.expedia.www.haystack.attributor.http.ManagedHttpService
import com.expedia.www.haystack.attributor.http.resource.{IsWorkingResource, ServiceStatsResource}
import com.expedia.www.haystack.attributor.operations.ESOperationsCountFetcher
import com.expedia.www.haystack.attributor.stream._
import com.expedia.www.haystack.attributor.stream.fetcher.{GlobalServiceStatsFetcher, LocalServiceStatsFetcher, StatsFetcher}
import com.expedia.www.haystack.commons.health.{HealthController, HealthStatusController}
import com.expedia.www.haystack.commons.kstreams.app.ManagedKafkaStreams
import com.expedia.www.haystack.commons.logger.LoggerUtils
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import org.apache.kafka.streams.KafkaStreams
import org.slf4j.LoggerFactory

/**
  * Starting point for application.
  * Loads configuration, creates stream and creates http service.
  */
object App extends MetricsSupport {
  private val LOGGER = LoggerFactory.getLogger(App.getClass)

  def main(args: Array[String]): Unit = {
    var app: ManagedApplication = null

    try {

      val appConfiguration = new AppConfiguration()

      // instantiate the application
      // if any exception occurs during instantiation
      // gracefully handles teardown and does system exit
      app = createApp(appConfiguration)

      // start the application
      // if any exception occurs during startup
      // gracefully handles teardown and does system exit
      app.start()

      // add a shutdown hook
      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run(): Unit = {
          LOGGER.info("Shutdown hook is invoked, tearing down the application.")
          app.stop()
        }
      })

      // mark the status of app as 'healthy'
      HealthController.setHealthy()
    } catch {
      case ex: Exception =>
        LOGGER.error("Observed fatal exception instantiating the app", ex)
        if (app != null) app.stop()
        LoggerUtils.shutdownLogger()
        System.exit(1)
    }
  }

  private def createApp(appConfig: AppConfiguration): ManagedApplication = {
    val jmxReporter: JmxReporter = JmxReporter.forRegistry(metricRegistry).build()
    val healthStatusController = new HealthStatusController

    val stream: KafkaStreams = createStream(appConfig, healthStatusController)

    val localFetcher = new LocalServiceStatsFetcher(stream, appConfig.kafkaConfig.storeName)
    val globalFetcher = new GlobalServiceStatsFetcher(stream, appConfig.kafkaConfig, appConfig.httpConfig, localFetcher)
    val serviceOperationsCountFetcher = new ESOperationsCountFetcher(appConfig.elasticSearchConfiguration)
    val httpService = createHttpService(appConfig, localFetcher, globalFetcher, serviceOperationsCountFetcher, healthStatusController)
    TagsOperator.apply(appConfig.tagsConfigList)

    healthStatusController.setHealthy()
    new ManagedApplication(new ManagedKafkaStreams(stream), httpService, jmxReporter)
  }

  private def createStream(appConfig: AppConfiguration, healthStatusController: HealthStatusController): KafkaStreams = {
    // service graph kafka stream supplier
    val topologySupplier = new ServiceStatsTopologySupplier(appConfig.kafkaConfig, appConfig.aggregateStatsConfig)

    // create kstream using application topology
    val streamSupplier = new StreamSupplier(
      topologySupplier,
      healthStatusController,
      appConfig.kafkaConfig.streamsConfig,
      appConfig.kafkaConfig.consumerTopic)

    // build kstream app
    streamSupplier.get()
  }

  private def createHttpService(appCfg: AppConfiguration,
                                localFetcher: StatsFetcher,
                                globalFetcher: StatsFetcher,
                                serviceOperationsCountFetcher: ESOperationsCountFetcher,
                                healthStatusController: HealthStatusController): ManagedHttpService = {
    val servlets = Map(
      "/services/stats" -> new ServiceStatsResource(
        localFetcher,
        globalFetcher,
        serviceOperationsCountFetcher),

      "/isWorking" -> new IsWorkingResource(() => healthStatusController.isHealthy)
    )

    new ManagedHttpService(appCfg.httpConfig, servlets)
  }
}