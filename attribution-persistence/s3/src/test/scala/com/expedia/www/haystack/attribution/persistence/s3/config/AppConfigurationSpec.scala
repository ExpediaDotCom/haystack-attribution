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

package com.expedia.www.haystack.attribution.persistence.s3.config

import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

class AppConfigurationSpec extends FunSpec with Matchers with EasyMockSugar {
  describe("AppConfigurationSpec") {
    it("should read all the configs from resource file") {
      val appConfig = new AppConfiguration()
      appConfig.environmentName should be("dev")

      appConfig.attributorConfig.host should be("attributor")
      appConfig.attributorConfig.scheme should be("http")
      appConfig.attributorConfig.scheme should be("http")
      appConfig.attributorConfig.path should be("/services/stats")
      appConfig.attributorConfig.connTimeout should be(10000)
      appConfig.attributorConfig.socketTimeout should be(10000)

      appConfig.persistS3Configs.items.head.enabled should be(true)
      appConfig.persistS3Configs.items.head.transformerId.head should be("t1")
      appConfig.persistS3Configs.items.head.bucket should be("haystack")
      appConfig.persistS3Configs.items.head.folderPath should be("attribution-dev/spans")
      appConfig.persistS3Configs.items.head.useStsRole should be(false)
      appConfig.persistS3Configs.items.head.stsRoleArn should be("arn:aws:iam::00000:role/XYZ")

      appConfig.transformersConfig.items.head.id should be("t1")
      appConfig.transformersConfig.items.head.classRelativePath should be("com.expedia.www.haystack.attribution.persistence.s3.transformer.ServiceStatsCsvTransformer")
      appConfig.transformersConfig.items.head.customTags("tagKey1") should be("tagValue1")
      appConfig.transformersConfig.items.head.customTags("tagKey2") should be("tagValue2")

      appConfig.transformers.head.getId should be("t1")
    }
  }
}
