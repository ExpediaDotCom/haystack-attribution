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

package com.expedia.www.haystack.attributor.config.entities

import org.apache.commons.lang3.StringUtils

/**
  * ES config for connection
  *
  * @param endpoint                ES endpoint
  * @param connectionTimeoutMillis timeout for connection to ES
  * @param readTimeoutMillis       read timeout for connection to ES
  */
case class ElasticSearchClientConfiguration(endpoint: String,
                                            connectionTimeoutMillis: Int,
                                            readTimeoutMillis: Int) {
  require(StringUtils.isNotBlank(endpoint))
  require(connectionTimeoutMillis > 0)
  require(readTimeoutMillis > 0)
}

/**
  * ES connection and index config
  *
  * @param clientConfiguration      connection related configs
  * @param serviceMetadataIndexName ES index name which contains service-operation data
  */
case class ElasticSearchConfiguration(clientConfiguration: ElasticSearchClientConfiguration,
                                      serviceMetadataIndexName: String) {
  require(clientConfiguration != null)
  require(StringUtils.isNotBlank(serviceMetadataIndexName))
}
