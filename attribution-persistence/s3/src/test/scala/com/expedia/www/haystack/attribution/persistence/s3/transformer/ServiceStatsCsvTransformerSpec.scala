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

import java.util.Date

import com.expedia.www.haystack.attribution.commons.entities.{AttributedTagValue, ServiceStats, ValueType}
import com.expedia.www.haystack.attribution.persistence.s3.config.AppConfiguration
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

import scala.io.Source

class ServiceStatsCsvTransformerSpec extends FunSpec with Matchers with EasyMockSugar {
  describe("ServiceStatsCsvTransformerSpec") {
    val appConfig = new AppConfiguration
    val transformer = appConfig.transformers.head

    it("should successfully create a valid csv string") {

      val resultCsv = Source.fromResource("statsCsvResultWithTags.csv").mkString

      val date1 = new Date(1545828700000l) // GMT: Wednesday, December 26, 2018 12:51:40 PM
      val serviceStats1 = ServiceStats("service1",
        2,
        200,
        1,
        date1,
        Map("attribute_name_1" -> AttributedTagValue("111", ValueType.COUNT.toString), "attribute_name_2" -> AttributedTagValue("222", ValueType.BYTES.toString)),
        4
      )

      val date2 = new Date(1545822700000l) // GMT: Wednesday, December 26, 2018 11:11:40 AM
      val serviceStats2 = ServiceStats("service2",
        5,
        1000,
        3,
        date2,
        Map("attribute_name_1" -> AttributedTagValue("333", ValueType.COUNT.toString), "attribute_name_2" -> AttributedTagValue("444", ValueType.BYTES.toString)),
        80
      )

      val statsSimpleCsvString = transformer.transform(List(serviceStats1, serviceStats2))
      statsSimpleCsvString should equal(resultCsv)
    }
  }
}