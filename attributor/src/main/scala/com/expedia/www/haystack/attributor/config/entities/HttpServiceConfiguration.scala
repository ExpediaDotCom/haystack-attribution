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
  * Service related config including connection config
  *
  * @param host          host name
  * @param port          port on which you want it to run
  * @param minThreads    thread pool config for minimum threads
  * @param maxThreads    thread pool config for maximum threads
  * @param idleTimeout   thread pool config for idle timeout
  * @param connTimeout   connection timeout for service
  * @param socketTimeout socket timeout for service
  */
case class HttpServiceConfiguration(host: String,
                                    port: Int,
                                    minThreads: Int,
                                    maxThreads: Int,
                                    idleTimeout: Int,
                                    connTimeout: Int,
                                    socketTimeout: Int) {
  require(StringUtils.isNotEmpty(host))
  require(port > 0)
  require(minThreads > 0)
  require(maxThreads > minThreads)
  require(idleTimeout > 0)
  require(connTimeout > 0)
  require(socketTimeout > 0)
}
