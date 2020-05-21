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

import com.expedia.www.haystack.attribution.commons.entities.ServiceStats
import com.expedia.www.haystack.attribution.persistence.s3.config.AppConfiguration
import org.apache.http.client.fluent.Request
import org.apache.http.client.utils.URIBuilder
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization

/**
  * Fetcher for retrieving attribution data from attributor service
  */
object StatsFetcher {
  implicit val formats = DefaultFormats

  def fetch(appConfiguration: AppConfiguration, from: Long, to: Long): List[ServiceStats] = {
    val request = new URIBuilder()
      .setScheme(appConfiguration.attributorConfig.scheme)
      .setPath(appConfiguration.attributorConfig.path)
      .setParameter("from", from.toString)
      .setParameter("to", to.toString)
      .setHost(appConfiguration.attributorConfig.host)
      .setPort(appConfiguration.attributorConfig.port)
      .build()

    val response = Request.Get(request)
      .connectTimeout(appConfiguration.attributorConfig.connTimeout)
      .socketTimeout(appConfiguration.attributorConfig.socketTimeout)
      .execute()
      .returnContent()
      .asString()

    Serialization.read[List[ServiceStats]](response)
  }
}
