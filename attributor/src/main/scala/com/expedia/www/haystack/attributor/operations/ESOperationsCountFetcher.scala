/*
 *  Copyright 2020 Expedia Group
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */

package com.expedia.www.haystack.attributor.operations

import java.net.{HttpURLConnection, URL}

import com.expedia.www.haystack.attributor.config.entities.ElasticSearchConfiguration
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.slf4j.LoggerFactory

/**
  * Fetcher for getting operation count for a service from ES at that moment
  *
  * @param config config for ES connection
  */
class ESOperationsCountFetcher(config: ElasticSearchConfiguration) {
  private val LOGGER = LoggerFactory.getLogger(classOf[ESOperationsCountFetcher])

  private val esOperationsCountQuery =
    """
      |{
      |  "query" : {
      |    "term" : {
      |      "servicename" : {
      |        "value" : "<service_name>",
      |        "boost" : 1.0
      |      }
      |    }
      |  }
      |}
    """.stripMargin

  // initialize the elastic search client
  private def executeESQuery(esQueryString: String): Int = {
    import java.io.{BufferedReader, InputStreamReader}
    var esOperationsCountResult = ESOperationsCountResult(0, "")
    val esCountQueryEndpoint = getESCountQueryEndpoint
    try {
      val url = new URL(esCountQueryEndpoint)
      val httpURLConnection = url.openConnection().asInstanceOf[HttpURLConnection]
      httpURLConnection.setDoOutput(true)
      httpURLConnection.setRequestMethod("POST")
      httpURLConnection.setRequestProperty("Content-Type", "application/json")
      httpURLConnection.setConnectTimeout(config.clientConfiguration.connectionTimeoutMillis)
      httpURLConnection.setReadTimeout(config.clientConfiguration.readTimeoutMillis)

      val outputStream = httpURLConnection.getOutputStream
      outputStream.write(esQueryString.getBytes())
      outputStream.flush()

      if (httpURLConnection.getResponseCode != HttpURLConnection.HTTP_OK) {
        throw new RuntimeException("Failed to query ES for operations count. HTTP error code : "
          + httpURLConnection.getResponseCode)
      }

      val bufferedReader = new BufferedReader(new InputStreamReader(
        httpURLConnection.getInputStream))

      val responseString = Stream.continually(bufferedReader.readLine()).takeWhile(_ != null).mkString
      esOperationsCountResult = jsonStringToCountResult(responseString)
      bufferedReader.close()
    } catch {
      case ex: Exception => LOGGER.error("Exception occurred while fetching operations count from ES. Query=" + esCountQueryEndpoint, ex)
    }
    esOperationsCountResult.count
  }

  def jsonStringToCountResult(responseString: String): ESOperationsCountResult = {
    val objectMapper = new ObjectMapper with ScalaObjectMapper
    objectMapper.registerModule(DefaultScalaModule)
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.setSerializationInclusion(Include.NON_NULL)
    objectMapper.readValue(responseString, classOf[ESOperationsCountResult])
  }

  def countOperations(serviceName: String): Long = {
    executeESQuery(esOperationsCountQuery.replace("<service_name>", serviceName))
  }

  def getESCountQueryEndpoint: String = {
    config.clientConfiguration.endpoint + "/" + config.serviceMetadataIndexName + "/_count"
  }
}