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

import com.expedia.www.haystack.attribution.commons.entities.ServiceStats
import com.typesafe.config.Config

/**
  * Contract for transforming attributed service stats data to string to be written to AWS s3 store
  */
trait ServiceStatsTransformer {

  def getId: String

  def init(id: String, customTags: Map[String, String], config: Config): Unit

  def transform(serviceStats: List[ServiceStats]): String
}
