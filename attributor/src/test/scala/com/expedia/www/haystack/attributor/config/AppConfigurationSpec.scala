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

package com.expedia.www.haystack.attributor.config

import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

class AppConfigurationSpec extends FunSpec with Matchers with EasyMockSugar {
  describe("AppConfigurationSpec") {
    it("should read all the configs from resource file") {
      val appConfig = new AppConfiguration()
      appConfig.healthStatusFilePath should be("/app/isHealthy")
      appConfig.kafkaConfig.storeName should be("haystack-attributor")
      appConfig.aggregateStatsConfig.retentionDays should be(2)
      appConfig.httpConfig.host should be("localhost")
      appConfig.elasticSearchConfiguration.clientConfiguration.endpoint should be("http://elasticsearch:9200")
      appConfig.elasticSearchConfiguration.serviceMetadataIndexName should be("service-metadata")
      appConfig.tagsConfigList.size should be(4)
      appConfig.tagsConfigList.head.attributeName should be("attribute_name_1")
      appConfig.tagsConfigList(1).spanTagKeys.head should be("tag_key_3")
    }
  }
}