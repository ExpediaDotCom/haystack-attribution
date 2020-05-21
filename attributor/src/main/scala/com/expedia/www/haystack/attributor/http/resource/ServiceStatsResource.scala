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

package com.expedia.www.haystack.attributor.http.resource

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import com.expedia.www.haystack.attribution.commons.entities.ServiceStats
import com.expedia.www.haystack.attributor.operations.ESOperationsCountFetcher
import com.expedia.www.haystack.attributor.stream.fetcher.StatsFetcher
import javax.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

/**
  * Service Stats servlet. This will be used to fetch attributed data
  *
  * @param localFetcher                  local fetcher to get the service stats
  * @param globalFetcher                 global fetcher to get the service stats and merge them
  * @param serviceOperationsCountFetcher fetcher to get operations count for each service
  */
class ServiceStatsResource(localFetcher: StatsFetcher,
                           globalFetcher: StatsFetcher,
                           serviceOperationsCountFetcher: ESOperationsCountFetcher)
  extends BaseResource("service.stats.resource") {
  private val LOGGER = LoggerFactory.getLogger(classOf[ServiceStatsResource])
  private val DATE_FORMAT = "yyyyMMdd"

  protected override def get(request: HttpServletRequest): List[ServiceStats] = {
    val from = fromTimestamp(request)
    val to = toTimestamp(request)

    if (isGlobalQuery(request)) {
      val results = mergeVirtualServiceNodes(globalFetcher.fetch(from, to))
      val sortedResult = results.sortBy(stats => -stats.spanCount)

      sortedResult.foreach(result => {
        result.updateOperationCount(serviceOperationsCountFetcher.countOperations(result.serviceName))
      })

      if (sortedResult.isEmpty) {
        throw new RuntimeException("no service stats are found for http request, throwing error instead of empty response")
      }

      LOGGER.info("Returning successful result for global query")
      sortedResult
    } else {
      LOGGER.info("Returning successful result for local query")
      localFetcher.fetch(from, to)
    }
  }

  private def isGlobalQuery(request: HttpServletRequest): Boolean = {
    StringUtils.isEmpty(request.getParameter("local")) || !request.getParameter("local").toBoolean
  }

  private def fromTimestamp(request: HttpServletRequest): Long = {
    if (StringUtils.isEmpty(request.getParameter("from"))) {
      Instant.now().minus(24, ChronoUnit.HOURS).toEpochMilli
    } else {
      extractTime(request, "from")
    }
  }

  private def toTimestamp(request: HttpServletRequest): Long = {
    if (StringUtils.isEmpty(request.getParameter("to"))) {
      Instant.now().toEpochMilli
    } else {
      extractTime(request, "to")
    }
  }

  private def extractTime(request: HttpServletRequest, key: String): Long = {
    val paramVal = request.getParameter(key)
    try {
      if (paramVal.length == DATE_FORMAT.length) {
        val dt = new SimpleDateFormat(DATE_FORMAT).parse(paramVal)
        val epochMillis = dt.toInstant.toEpochMilli
        val days: Long = epochMillis / TimeUnit.DAYS.toMillis(1)
        days * TimeUnit.DAYS.toMillis(1)
      } else {
        paramVal.toLong
      }
    } catch {
      case _: Exception => paramVal.toLong
    }
  }

  private def mergeVirtualServiceNodes(stats: List[ServiceStats]): List[ServiceStats] = {
    stats
      .groupBy(_.serviceName)
      .values
      .map(_.reduce(_ + _))
      .toList
  }
}