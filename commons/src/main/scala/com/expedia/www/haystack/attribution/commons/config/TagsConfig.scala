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

package com.expedia.www.haystack.attribution.commons.config

import com.expedia.www.haystack.attribution.commons.entities.{TagsOperatorType, ValueType}
import org.apache.commons.lang3.Validate

/**
  * Collection of TagsConfig
  *
  * @param items Collection of TagsConfig
  */
case class TagsConfigList(items: Seq[TagsConfig])

/**
  * Tags config for span tags and attributed fields details
  *
  * @param attributeName        Name which you want to keep for the field in attribution report
  * @param spanTagKeys          One or more key for the span tags which needs to be attributed under above attributeName
  * @param operatorType         Type of operation that needs to be performed. COUNT / SUM / BAGGAGE
  * @param defaultValue         If span doesn't contain the tag then use default value
  * @param valueType            have a look at the ServiceStats class for its value
  * @param valueWhitelistString Optional. If we want to attribute only certain spans with values which contains
  *                             valueWhitelistString. This filter condition is applied on matching spanTagKeys
  */
case class TagsConfig(attributeName: String,
                      spanTagKeys: Seq[String],
                      operatorType: String,
                      defaultValue: String,
                      valueType: String,
                      valueWhitelistString: String) {
  Validate.notEmpty(attributeName)
  Validate.notNull(spanTagKeys)
  Validate.notEmpty(operatorType)
  Validate.notEmpty(defaultValue)
  Validate.notEmpty(valueType)

  try {
    TagsOperatorType.withName(operatorType)
  } catch {
    case _: Throwable => throw new RuntimeException("Invalid operatorType in tags config. Kindly check the tags section in your app config.")
  }

  try {
    ValueType.withName(valueType)
  } catch {
    case _: Throwable => throw new RuntimeException("Invalid valueType in tags config. Kindly check the tags section in your app config.")
  }
}

object TagsConfig {
  def getOperatorTypeByAttributeName(attrName: String, tagsConfigList: Seq[TagsConfig]): String = {
    val matchedTagsConfig = tagsConfigList
      .find(tagsConfig => tagsConfig.attributeName.equalsIgnoreCase(attrName))

    matchedTagsConfig match {
      case Some(tagsConfig) => tagsConfig.operatorType
      case _ => "UNKNOWN"
    }
  }
}