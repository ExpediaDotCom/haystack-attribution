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

package com.expedia.www.haystack.attributor.stream.fetcher

import com.expedia.www.haystack.attribution.commons.entities.ServiceStats
import com.expedia.www.haystack.attributor.config.entities.{HttpServiceConfiguration, KafkaConfiguration}
import org.apache.http.client.fluent.Request
import org.apache.http.client.utils.URIBuilder
import org.apache.kafka.streams.KafkaStreams
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * Read from all kafka streams and return service stats as a collection
  *
  * @param streams      kafka streams to get service stats
  * @param kafkaConfig  kafka config
  * @param httpConfig   http config
  * @param localFetcher to fetch from the local instance
  */
class GlobalServiceStatsFetcher(streams: KafkaStreams,
                                kafkaConfig: KafkaConfiguration,
                                httpConfig: HttpServiceConfiguration,
                                localFetcher: LocalServiceStatsFetcher) extends StatsFetcher {

  private val LOGGER = LoggerFactory.getLogger(classOf[GlobalServiceStatsFetcher])

  implicit val formats = DefaultFormats

  def fetch(from: Long, to: Long = System.currentTimeMillis()): List[ServiceStats] = {
    streams
      .allMetadataForStore(kafkaConfig.storeName).asScala
      .flatMap(metadata => {
        LOGGER.info(s"local metadata.host= ${metadata.host()} httpConfig.host ${httpConfig.host}")
        if (metadata.host() == httpConfig.host) {
          val localServiceStats = localFetcher.fetch(from, to)
          LOGGER.info(s"local returned ${localServiceStats.size} service stats")
          localServiceStats
        }
        else {
          LOGGER.info("Fetching service stats from the host {} and port {}", metadata.host(), metadata.port())
          val remoteServiceStats = fetch(metadata.host(), metadata.port(), from, to)
          LOGGER.info(s"remote host '${metadata.host()}' returned ${remoteServiceStats.size} services stats")
          remoteServiceStats
        }
      }).toList
  }

  private def fetch(host: String, port: Int, from: Long, to: Long): List[ServiceStats] = {
    val request = new URIBuilder()
      .setScheme("http")
      .setPath("/services/stats")
      .setParameter("from", from.toString)
      .setParameter("to", to.toString)
      .setParameter("local", "true")
      .setHost(host)
      .setPort(port)
      .build()

    val response = Request.Get(request)
      .connectTimeout(httpConfig.connTimeout)
      .socketTimeout(httpConfig.socketTimeout)
      .execute()
      .returnContent()
      .asString()

    Serialization.read[List[ServiceStats]](response)
  }
}