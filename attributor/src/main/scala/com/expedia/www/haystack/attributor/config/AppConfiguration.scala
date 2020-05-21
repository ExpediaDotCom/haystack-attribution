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

package com.expedia.www.haystack.attributor.config

import java.util.Properties

import com.expedia.www.haystack.attribution.commons.config.{TagsConfig, TagsConfigList}
import com.expedia.www.haystack.attributor.config.entities._
import com.expedia.www.haystack.commons.config.ConfigurationLoader
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.config.{Config, ConfigRenderOptions}
import org.apache.commons.lang3.StringUtils
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology.AutoOffsetReset
import org.apache.kafka.streams.processor.TimestampExtractor
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * This class reads the configuration from the given resource name using {@link ConfigurationLoader ConfigurationLoader}
  */
class AppConfiguration() {
  private val LOGGER = LoggerFactory.getLogger(classOf[AppConfiguration])

  private val config = ConfigurationLoader.loadConfigFileWithEnvOverrides()

  LOGGER.info(config.root()
    .render(ConfigRenderOptions.defaults().setOriginComments(false))
    .replaceAll("(?i)(\\\".*password\\\"\\s*:\\s*)\\\".+\\\"", "$1********"))

  /**
    * Location of the health status file
    */
  val healthStatusFilePath: String = config.getString("health.status.path")

  /**
    * Returns instance of {@link KafkaConfiguration KafkaConfiguration} to be used by the kstreams application
    */
  lazy val kafkaConfig: KafkaConfiguration = {

    // verify if the applicationId and bootstrap server config are non empty
    def verifyRequiredProps(props: Properties): Unit = {
      require(StringUtils.isNotBlank(props.getProperty(StreamsConfig.APPLICATION_ID_CONFIG)))
      require(StringUtils.isNotBlank(props.getProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)))
      require(StringUtils.isNotBlank(props.getProperty(StreamsConfig.APPLICATION_SERVER_CONFIG)))
    }

    def addProps(config: Config, props: Properties, prefix: (String) => String = identity): Unit = {
      config.entrySet().asScala.foreach(kv => {
        val propKeyName = prefix(kv.getKey)
        props.setProperty(propKeyName, kv.getValue.unwrapped().toString)
      })
    }

    val kafka = config.getConfig("kafka")
    val streamsConfig = kafka.getConfig("streams")
    val consumerConfig = kafka.getConfig("consumer")

    // add stream specific properties
    val streamProps = new Properties
    addProps(streamsConfig, streamProps)

    // add stream application server config
    streamProps.setProperty(StreamsConfig.APPLICATION_SERVER_CONFIG,
      s"${config.getString("service.host")}:${config.getInt("service.port")}")

    // validate props
    verifyRequiredProps(streamProps)

    // offset reset for kstream
    val autoOffsetReset =
      if (streamsConfig.hasPath("auto.offset.reset"))
        AutoOffsetReset.valueOf(streamsConfig.getString("auto.offset.reset").toUpperCase)
      else
        AutoOffsetReset.LATEST

    streamProps.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, classOf[CustomRocksDBConfig])

    val timestampExtractor = Class.forName(streamProps.getProperty("timestamp.extractor",
      "org.apache.kafka.streams.processor.WallclockTimestampExtractor"))
      .newInstance().asInstanceOf[TimestampExtractor]

    KafkaConfiguration(new StreamsConfig(streamProps),
      consumerConfig.getString("topic"),
      autoOffsetReset,
      kafka.getLong("close.timeout.ms"),
      kafka.getString("storename"),
      timestampExtractor
    )
  }

  /**
    * Returns instance of {@link HttpServiceConfiguration HttpServiceConfiguration} for running attributor as a service
    */
  lazy val httpConfig: HttpServiceConfiguration = {
    val service = config.getConfig("service")
    val threads = service.getConfig("threads")
    val client = service.getConfig("client")

    HttpServiceConfiguration(
      service.getString("host"),
      service.getInt("port"),
      threads.getInt("min"),
      threads.getInt("max"),
      service.getInt("idle.timeout.ms"),
      client.getInt("connection.timeout.ms"),
      client.getInt("socket.timeout.ms"))
  }

  /**
    * Returns instance of {@link AggregateStatsConfiguration AggregateStatsConfiguration} for aggregation related config
    */
  lazy val aggregateStatsConfig: AggregateStatsConfiguration = {
    val cfg = config.getConfig("aggregate.stats")
    AggregateStatsConfiguration(cfg.getInt("window.sec"), cfg.getInt("retention.days"), cfg.getInt("vnodes"))
  }

  /**
    * Returns instance of {@link ElasticSearchConfiguration ElasticSearchConfiguration} for ES config
    */
  lazy val elasticSearchConfiguration: ElasticSearchConfiguration = {
    ElasticSearchConfiguration(elasticSearchClientConfig, config.getString("elasticsearch.index.service.metadata.name"))
  }

  /**
    * Returns instance of collection of {@link TagsConfig TagsConfig} for attribution fields
    */
  lazy val tagsConfigList: Seq[TagsConfig] = {
    if (config.hasPath("tags.json") && StringUtils.isNoneEmpty(config.getString("tags.json"))) {
      val jsonString = config.getString("tags.json")
      val objectMapper = new ObjectMapper with ScalaObjectMapper
      objectMapper.registerModule(DefaultScalaModule)
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      objectMapper.setSerializationInclusion(Include.NON_NULL)

      val tags: TagsConfigList = objectMapper.readValue(jsonString, classOf[TagsConfigList])
      tags.items
    } else {
      Seq()
    }
  }

  private val elasticSearchClientConfig: ElasticSearchClientConfiguration = {
    val es = config.getConfig("elasticsearch.client")

    ElasticSearchClientConfiguration(
      endpoint = es.getString("endpoint"),
      connectionTimeoutMillis = es.getInt("conn.timeout.ms"),
      readTimeoutMillis = es.getInt("read.timeout.ms")
    )
  }
}
