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

import javax.servlet.http.HttpServletRequest

case class WorkingStatus(isWorking: Boolean)

/**
  * Is working servlet for checking app's health
  *
  * @param isHealthy is healthy flag
  */
class IsWorkingResource(isHealthy: () => Boolean) extends BaseResource("isworking") {
  private val healthyWorkStatus = WorkingStatus(isWorking = true)

  protected override def get(request: HttpServletRequest): WorkingStatus = {
    if (isHealthy()) healthyWorkStatus else throw new RuntimeException("App is not healthy!!!")
  }
}