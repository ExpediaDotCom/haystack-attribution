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

/**
  * Config for details of aggregation
  *
  * @param windowSec     window size of the attributed data. Like 300 (5min) / 900 (15min)
  * @param retentionDays retention period. For how many days we want to keep the attributed data
  * @param vNodes        This is used for creating a random key for kafka
  */
case class AggregateStatsConfiguration(windowSec: Int, retentionDays: Int, vNodes: Int) {
  require(windowSec > 0)
  require(retentionDays > 0)
  require(vNodes > 0)
}
