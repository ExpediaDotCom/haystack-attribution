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

package com.expedia.www.haystack.attribution.persistence.s3.config.entities

/**
  * Details of attribution service for fetching attributed data to be persisted to s3
  *
  * @param host          host to connect
  * @param path          path after hostname
  * @param port          port to connect
  * @param connTimeout   connection timeout
  * @param socketTimeout socket timeout
  */
case class AttributorConfiguration(scheme: String,
                                   host: String,
                                   path: String,
                                   port: Int,
                                   connTimeout: Int,
                                   socketTimeout: Int)