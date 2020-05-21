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

package com.expedia.www.haystack.attribution.persistence.email.config

import com.expedia.www.haystack.attribution.persistence.email.config.entities.AwsEmailNotifyConfiguration
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

class AppConfigurationSpec extends FunSpec with Matchers with EasyMockSugar {
  describe("AppConfigurationSpec") {
    it("should read all the configs from resource file") {
      val appConfig = new AppConfiguration()
      appConfig.environmentName should be ("dev")

      appConfig.emailNotifyConfig.asInstanceOf[AwsEmailNotifyConfiguration].awsRegion should be ("us-west-2")
      appConfig.emailNotifyConfig.asInstanceOf[AwsEmailNotifyConfiguration].fromEmailAddr should be ("from@test.com")
      appConfig.emailNotifyConfig.asInstanceOf[AwsEmailNotifyConfiguration].toEmailAddr.size should be (2)
      appConfig.emailNotifyConfig.asInstanceOf[AwsEmailNotifyConfiguration].toEmailAddr.toSet should be (Set("to1@test.com", "to2@test.com"))
      appConfig.emailNotifyConfig.asInstanceOf[AwsEmailNotifyConfiguration].overrideSubjectLine should be ("[Haystack Attribution] Daily Usage Report")

      appConfig.attributorConfig.host should be ("attributor")
      appConfig.attributorConfig.path should be ("/services/stats")
    }
  }
}
