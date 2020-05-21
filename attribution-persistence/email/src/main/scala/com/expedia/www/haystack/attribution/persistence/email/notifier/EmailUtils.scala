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

package com.expedia.www.haystack.attribution.persistence.email.notifier

import com.expedia.www.haystack.attribution.commons.Utils
import com.expedia.www.haystack.attribution.commons.entities.{ServiceStats, ValueType}

/**
  * Class to generate html for attribution data
  */
object EmailUtils {

  def getEmailBody(template: String, stats: List[ServiceStats]): String = {
    val tableRows = stats.zipWithIndex.map {
      case (stat, idx) =>
        s"""
           |<tr>
           |    <td headers="sequenceNo">${idx + 1}</td>
           |    <td headers="serviceName">${stat.serviceName}</td>
           |    <td headers="spanCount">${Utils.format(stat.spanCount.toString, ValueType.COUNT.toString)}</td>
           |    <td headers="spanSizeBytes">${Utils.format(stat.spanSizeBytes.toString, ValueType.BYTES.toString)}</td>
           |    <td headers="operationCount">${Utils.format(stat.operationCount.toString, ValueType.COUNT.toString)}</td>
           |${getAttributedTags(stat)}
           |</tr>
        """.stripMargin
    }.mkString("\n")

    template.replace("{{row}}", tableRows)
  }

  def getAttributedTags(stat: ServiceStats): String = {
    stat.attributedTags
      .map(attributedTag => s"<td headers='${attributedTag._1}'>${Utils.format(attributedTag._2)}</td>").mkString("\n")
  }
}