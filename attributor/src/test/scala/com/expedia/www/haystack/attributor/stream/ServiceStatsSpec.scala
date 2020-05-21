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

package com.expedia.www.haystack.attributor.stream

import java.util.Date

import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.attribution.commons.entities.{AttributedTagValue, ServiceStats, TagsOperator}
import com.expedia.www.haystack.attributor.config.AppConfiguration
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

class ServiceStatsSpec extends FunSpec with Matchers with EasyMockSugar {

  describe("ServiceStatsSpec with tags") {
    val appConfig = new AppConfiguration()
    TagsOperator.apply(appConfig.tagsConfigList)

    it("should add the ServiceStats as expected") {
      val serviceStats1 = ServiceStats("abc", 1, 11, 123, new Date(), Map("attribute_name_1" -> AttributedTagValue("10", "COUNT"), "attribute_name_2" -> AttributedTagValue("20", "BYTES")))
      val serviceStats2 = ServiceStats("abc", 2, 22, 123, new Date(), Map("attribute_name_1" -> AttributedTagValue("30", "COUNT"), "attribute_name_3" -> AttributedTagValue("50", "BYTES")))

      val summedStats = serviceStats1 + serviceStats2

      summedStats.spanCount should be(3)
      summedStats.spanSizeBytes should be(33)
      summedStats.attributedTags.size should be(3)
      summedStats.attributedTags("attribute_name_1") should be("40")
      summedStats.attributedTags("attribute_name_2") should be("20")
      summedStats.attributedTags("attribute_name_3") should be("50")
    }

    it("span tags should be evaluated as expected as per operator type") {
      val span1 = Span.newBuilder().addTags(Tag.newBuilder().setKey("tag_key_1").setVStr("some_dummy_data")).addTags(Tag.newBuilder().setKey("tag_key_2").setVBool(true)).addTags(Tag.newBuilder().setKey("tag_key_3").setVStr("https://haystack.com/serv_a")).build()
      val span2 = Span.newBuilder().addTags(Tag.newBuilder().setKey("tag_key_1").setVLong(2000)).addTags(Tag.newBuilder().setKey("tag_key_2").setVStr("adf")).addTags(Tag.newBuilder().setKey("tag_key_3").setVLong(20)).addTags(Tag.newBuilder().setKey("tag_key_99").setVStr("100")).build()

      val serviceStats1 = ServiceStats("abc",
        spanCount = 1,
        10,
        1,
        new Date(),
        ServiceStats.getStatsTags(span1))

      val serviceStats2 = ServiceStats("abc",
        spanCount = 1,
        10,
        1,
        new Date(),
        ServiceStats.getStatsTags(span2))

      val summedStats = serviceStats1 + serviceStats2

      summedStats.spanCount should be(2)
      summedStats.spanSizeBytes should be(20)
      summedStats.attributedTags.size should be(4)
      summedStats.attributedTags("attribute_name_1") should be("1")
      summedStats.attributedTags("attribute_name_2") should be("20")
      summedStats.attributedTags("attribute_name_3") should be("0")
      summedStats.attributedTags("attribute_name_4") should be("default_baggage")
    }

    it("span tags should be evaluated as expected as per operator type for baggage type") {
      val span1 = Span.newBuilder().addTags(Tag.newBuilder()).build()
      val span2 = Span.newBuilder().addTags(Tag.newBuilder().setKey("tag_key_7").setVStr("baggage_value")).build()

      val serviceStats1 = ServiceStats("abc",
        spanCount = 1,
        10,
        1,
        new Date(),
        ServiceStats.getStatsTags(span1))


      val serviceStats2 = ServiceStats("abc",
        spanCount = 1,
        10,
        1,
        new Date(),
        ServiceStats.getStatsTags(span2))

      val summedStats = serviceStats1 + serviceStats2

      summedStats.spanCount should be(2)
      summedStats.spanSizeBytes should be(20)
      summedStats.attributedTags.size should be(4)
      summedStats.attributedTags("attribute_name_1") should be("0")
      summedStats.attributedTags("attribute_name_2") should be("0")
      summedStats.attributedTags("attribute_name_3") should be("0")
      summedStats.attributedTags("attribute_name_4") should be("baggage_value")
    }

    it("should exclude spans with certain value from attribution") {
      val span1 = Span.newBuilder().addTags(Tag.newBuilder().setKey("tag_key_5").setVStr("123_only_include_me_456")).build()
      val span2 = Span.newBuilder().addTags(Tag.newBuilder().setKey("tag_key_5").setVLong(2000)).build()

      val serviceStats1 = ServiceStats("abc",
        spanCount = 1,
        10,
        1,
        new Date(),
        ServiceStats.getStatsTags(span1))


      val serviceStats2 = ServiceStats("abc",
        spanCount = 1,
        10,
        1,
        new Date(),
        ServiceStats.getStatsTags(span2))

      val summedStats = serviceStats1 + serviceStats2

      summedStats.attributedTags("attribute_name_3") should be("1")
    }
  }


  describe("ServiceStatsSpec without tags") {
    val appConfig = new AppConfiguration()
    TagsOperator.apply(appConfig.tagsConfigList)

    it("should add the ServiceStats as expected") {
      val serviceStats1 = ServiceStats("abc", 1, 11, 123, new Date(), Map("attribute_name_1" -> AttributedTagValue("10", "NONE"), "attribute_name_2" -> AttributedTagValue("20", "NONE")))
      val serviceStats2 = ServiceStats("abc", 2, 22, 123, new Date(), Map("attribute_name_1" -> AttributedTagValue("30", "NONE"), "attribute_name_3" -> AttributedTagValue("50", "NONE")))

      val summedStats = serviceStats1 + serviceStats2

      summedStats.spanCount should be(3)
      summedStats.spanSizeBytes should be(33)
      summedStats.attributedTags.size should be(3)
      summedStats.attributedTags("attribute_name_1") should be("40")
      summedStats.attributedTags("attribute_name_2") should be("20")
      summedStats.attributedTags("attribute_name_3") should be("50")
    }
  }
}