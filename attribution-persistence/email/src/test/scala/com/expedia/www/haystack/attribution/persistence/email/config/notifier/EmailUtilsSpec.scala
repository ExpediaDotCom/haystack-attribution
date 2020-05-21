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

package com.expedia.www.haystack.attribution.persistence.email.config.notifier

import java.util.Date

import com.expedia.www.haystack.attribution.commons.entities.{AttributedTagValue, ServiceStats, ValueType}
import com.expedia.www.haystack.attribution.persistence.email.config.AppConfiguration
import com.expedia.www.haystack.attribution.persistence.email.notifier.EmailUtils
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

import scala.io.Source

class EmailUtilsSpec extends FunSpec with Matchers with EasyMockSugar {
  describe("EmailUtilsSpec") {
    it("should generate valid email html when stats without tags are provided") {
      val resultHtml = Source.fromResource("email_html_without_tags").mkString
      val appConfiguration = new AppConfiguration()

      val serviceStats1 = ServiceStats("service1",
        20000,
        20000,
        1,
        new Date(),
        Map(),
        4
      )

      val serviceStats2 = ServiceStats("service2",
        5001000,
        1000000,
        3,
        new Date(),
        Map(),
        80000
      )

      val stats = List(serviceStats1, serviceStats2)
      val generatedEmailHtml = EmailUtils.getEmailBody(appConfiguration.emailTemplate, stats)
      generatedEmailHtml.replaceAll("    ", "") should equal(resultHtml)
    }

    it("should generate valid email html when stats with tags are provided") {
      val resultHtml = Source.fromResource("email_html_with_tags").mkString
      val overrideEmailTemplate = "<html>\n<head>\n<style>\ntable, th, td {\n    border: solid;\n    border-width: 1px;\n    border-collapse: collapse;\n}\nth, td {\n    padding: 5px;\n    text-align: left;\n}\n</style>\n</head>\n\nHello Team,\n<br>\n<br>\nPlease find the daily usage report:\n<br>\n<br>\n<table>\n <tr>\n    <th id=\"sequenceNo\">No.</th>\n    <th id=\"serviceName\">Service Name</th>\n    <th id=\"spanCount\">Span Count</th>\n    <th id=\"spanSizeBytes\">Span Size</th>\n    <th id=\"operationCount\">Operations Count</th>\n    <th id=\"tagKey1\">Attributed Tag 1</th>\n</tr>\n{{row}}\n</table>\n\n<br>\nThanks,\n<br>\nHaystack Bot\n</html>"

      val serviceStats1 = ServiceStats("service1",
        20000,
        20000,
        1,
        new Date(),
        Map("tagKey1" -> AttributedTagValue("123000000", ValueType.COUNT.toString)),
        4
      )

      val serviceStats2 = ServiceStats("service2",
        5001000,
        1000000,
        3,
        new Date(),
        Map("tagKey1" -> AttributedTagValue("6000000", ValueType.COUNT.toString)),
        80000
      )

      val stats = List(serviceStats1, serviceStats2)
      val generatedEmailHtml = EmailUtils.getEmailBody(overrideEmailTemplate, stats)
      generatedEmailHtml.replace("    ", "") should equal(resultHtml)
    }
  }
}