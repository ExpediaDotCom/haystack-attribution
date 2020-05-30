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

package com.expedia.www.haystack.attribution.persistence.s3.transformer

import java.text.SimpleDateFormat
import java.util.TimeZone

import com.expedia.www.haystack.attribution.commons.config.{TagsConfig, TagsConfigList}
import com.expedia.www.haystack.attribution.commons.entities.{AttributedTagValue, ServiceStats, TagsOperator, ValueType}
import com.expedia.www.haystack.attribution.persistence.s3.cost.CostCalculation
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.config.Config
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

/**
  * Transformer class for transforming attributed service stats to a simple format csv string for writing it to AWS s3
  */
class ServiceStatsCsvTransformer extends ServiceStatsTransformer {
  private val LOGGER = LoggerFactory.getLogger(classOf[ServiceStatsCsvTransformer])
  private val TOTAL_SERVICE_NAME = "Total"
  private val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z"
  private var environmentName: String = _
  private var tagsConfigList: Seq[TagsConfig] = _
  private var tagKeyNames: List[String] = _
  private var customTags: Map[String, String] = _
  private var id: String = _

  override def transform(serviceStats: List[ServiceStats]): String = {

    val allServiceCombined = serviceStats.reduce(_ + _).copy(serviceName = TOTAL_SERVICE_NAME, lastSeen = null)

    val dateFormat = new SimpleDateFormat(DATE_FORMAT)
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))

    val csvStringBuilder: StringBuilder = new StringBuilder

    // prepare the csv file header
    csvStringBuilder ++= "serviceName,spanCount,spanSizeBytes,lastSeen,operationCount,spanShare,env"
    csvStringBuilder.append(if (tagKeyNames.isEmpty) "" else "," + tagKeyNames.mkString(","))
    csvStringBuilder.append(if (customTags.isEmpty) "" else "," + customTags.keys.mkString(","))

    csvStringBuilder.append(System.getProperty("line.separator"))

    try {
      serviceStats.map(stats => {
        csvStringBuilder.append(
          stats.serviceName + "," +
            stats.spanCount + "," +
            stats.spanSizeBytes + "," +
            dateFormat.format(stats.lastSeen) + "," +
            stats.operationCount + "," +
            CostCalculation.getCapacityShare(allServiceCombined.spanSizeBytes, allServiceCombined.spanCount, stats.spanSizeBytes, stats.spanCount) + "," +
            environmentName)

        // append tag values
        val tagValues = extractValuesForTagKeys(stats)
        csvStringBuilder.append(if (tagValues.isEmpty) "" else  "," + tagValues.mkString(",") )

        // append custom field values
        csvStringBuilder.append(if (customTags.isEmpty) "" else "," + customTags.values.mkString(","))
        csvStringBuilder.append(System.getProperty("line.separator"))
      })
    } catch {
      case ex: Exception => LOGGER.error("Failed to transform data with error:", ex)
    }
    csvStringBuilder.toString
  }

  private def extractValuesForTagKeys(serviceStat: ServiceStats): List[String] = {
    tagKeyNames.map(key => {
      serviceStat.attributedTags.getOrElse(key,
        AttributedTagValue(tagsConfigList.filter(_.attributeName.equalsIgnoreCase(key)).head.defaultValue, ValueType.NONE.toString)).tagValue.toString
    })
  }

  override def init(id: String, customTags: Map[String, String], config: Config): Unit = {
    this.id = id
    this.customTags = if (customTags == null) Map() else customTags

    this.tagsConfigList = {
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

    this.tagKeyNames = tagsConfigList.map(_.attributeName).toList

    this.environmentName = config.getString("environment.name")

    TagsOperator.apply(tagsConfigList)
  }

  override def getId: String = this.id
}