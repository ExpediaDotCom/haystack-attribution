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

package com.expedia.www.haystack.attribution.commons.entities

import com.expedia.open.tracing.Tag
import com.expedia.www.haystack.attribution.commons.config.TagsConfig

import scala.collection.mutable
import scala.util.Try

/**
  * Object for implementation of supported operations those can be performed on span tags. Example for currently
  * supported operations are COUNT, SUM and BAGGAGE
  */
//TODO: make TagsOperator a class
object TagsOperator {
  var tagsConfigList: Seq[TagsConfig] = _

  def apply(tagsConfigList: Seq[TagsConfig]): Unit = {
    this.tagsConfigList = tagsConfigList
  }

  def eval(spanTags: Seq[Tag]): mutable.Map[String, AttributedTagValue] = {
    val statsMap: mutable.Map[String, AttributedTagValue] = mutable.Map()

    tagsConfigList.foreach(tagsConfig => {

      val matchedTags = spanTags
        .filter(spanTag => tagsConfig.spanTagKeys.contains(spanTag.getKey))
        .filter(spanTag => {
          if (tagsConfig.valueWhitelistString != null)
            spanTag.getVStr.toLowerCase.contains(tagsConfig.valueWhitelistString.toLowerCase)
          else
            true
        })

      TagsOperatorType.withName(tagsConfig.operatorType.toUpperCase) match {
        case TagsOperatorType.COUNT =>
          if (matchedTags.isEmpty) {
            statsMap += (tagsConfig.attributeName -> AttributedTagValue(tagsConfig.defaultValue, tagsConfig.valueType))
          } else {
            matchedTags
              .foreach {
                _ => statsMap += (tagsConfig.attributeName -> statsMap.getOrElse(tagsConfig.attributeName, AttributedTagValue(tagsConfig.defaultValue, tagsConfig.valueType)).incrementValue)
              }
          }
        case TagsOperatorType.SUM =>
          if (matchedTags.isEmpty) {
            statsMap += (tagsConfig.attributeName -> AttributedTagValue(tagsConfig.defaultValue, tagsConfig.valueType))
          } else {
            matchedTags.foreach {
              matchedTag =>
                statsMap += (tagsConfig.attributeName -> (AttributedTagValue(Try(matchedTag.getVLong).getOrElse(tagsConfig.defaultValue).toString, tagsConfig.valueType) + statsMap.get(tagsConfig.attributeName)))
            }
          }
        case TagsOperatorType.BAGGAGE =>
          if (matchedTags.isEmpty) {
            statsMap.getOrElseUpdate(tagsConfig.attributeName, AttributedTagValue(tagsConfig.defaultValue, tagsConfig.valueType))
          } else {
            statsMap += (tagsConfig.attributeName -> AttributedTagValue(matchedTags.head.getVStr, tagsConfig.valueType))
          }
      }
    })
    statsMap
  }

  def aggregateTags(spanOne: ServiceStats, spanTwo: ServiceStats): mutable.Map[String, AttributedTagValue] = {
    var aggregatedTags = mutable.Map[String, AttributedTagValue]()

    if (tagsConfigList != null && tagsConfigList.nonEmpty) {
      aggregatedTags ++= spanOne.attributedTags // add existing tags to new Map
      spanTwo.attributedTags.foreach {
        case (attributedKey, value) =>
          TagsOperatorType.withName(TagsConfig.getOperatorTypeByAttributeName(attributedKey, tagsConfigList).toUpperCase) match {

            case TagsOperatorType.COUNT | TagsOperatorType.SUM =>
              aggregatedTags += (attributedKey -> (value + aggregatedTags.get(attributedKey))) // add the values
            case TagsOperatorType.BAGGAGE =>
              aggregatedTags += (attributedKey -> value)
          }
      }
    }
    aggregatedTags
  }
}

object TagsOperatorType extends Enumeration {
  type OperatorType = Value
  val COUNT: TagsOperatorType.Value = Value("COUNT")
  val SUM: TagsOperatorType.Value = Value("SUM")
  val BAGGAGE: TagsOperatorType.Value = Value("BAGGAGE")
}