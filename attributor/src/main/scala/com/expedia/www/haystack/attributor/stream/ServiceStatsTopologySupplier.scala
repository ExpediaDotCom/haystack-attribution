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

package com.expedia.www.haystack.attributor.stream

import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.attribution.commons.entities.ServiceStats
import com.expedia.www.haystack.attributor.config.entities.{AggregateStatsConfiguration, KafkaConfiguration}
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.{Consumed, StreamsBuilder, Topology}

/**
  * KafkaStreams app topology supplier
  *
  * @param kafkaConfig     kafka config
  * @param aggrStatsConfig aggregation config
  */
class ServiceStatsTopologySupplier(kafkaConfig: KafkaConfiguration, aggrStatsConfig: AggregateStatsConfiguration) extends Supplier[Topology] {
  override def get(): Topology = initialize(new StreamsBuilder)

  private def initialize(builder: StreamsBuilder): Topology = {
    val spanStream: KStream[String, Array[Byte]] = builder.stream(
      kafkaConfig.consumerTopic,
      Consumed.`with`(Serdes.String(), Serdes.ByteArray(), kafkaConfig.timestampExtractor, kafkaConfig.autoOffsetReset))

    val serviceStatsStream: KStream[String, ServiceStats] = spanStream
      .mapValues((spanBytes: Array[Byte]) => initializeServiceStats(spanBytes))

    serviceStatsStream
      .groupBy((_: String, stats: ServiceStats) => generateKey(stats), Serialized.`with`(Serdes.String(), ServiceStats.serde()))
      .windowedBy(tumblingWindow())
      .reduce(reducer,
        Materialized
          .as(kafkaConfig.storeName)
          .withKeySerde(Serdes.String())
          .withValueSerde(ServiceStats.serde()))

    // build stream topology and return
    builder.build()
  }

  private def initializeServiceStats(spanBytes: Array[Byte]): ServiceStats = {
    val span = Span.parseFrom(spanBytes)

    val serviceStats = ServiceStats(span.getServiceName,
      1,
      spanBytes.length,
      vNodeId(span),
      new Date(),
      ServiceStats.getStatsTags(span))
    serviceStats
  }

  private def vNodeId(span: Span): Int = {
    Math.floorMod(span.getOperationName.hashCode, aggrStatsConfig.vNodes)
  }

  private def reducer(stats_1: ServiceStats, stats_2: ServiceStats): ServiceStats = stats_1 + stats_2

  private def tumblingWindow(): TimeWindows = {
    TimeWindows
      .of(TimeUnit.SECONDS.toMillis(aggrStatsConfig.windowSec))
      .until(TimeUnit.DAYS.toMillis(aggrStatsConfig.retentionDays))
  }

  private def generateKey(stats: ServiceStats): String = stats.serviceName + "." + stats.vNodeId
}