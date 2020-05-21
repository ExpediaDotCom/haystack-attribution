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

package com.expedia.www.haystack.attributor.tags

import com.expedia.open.tracing.Tag
import com.expedia.www.haystack.attribution.commons.entities.TagsOperator
import com.expedia.www.haystack.attributor.config.AppConfiguration
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

class TagsOperatorSpec extends FunSpec with Matchers with EasyMockSugar {

  describe("AppConfigurationSpec") {

    val appConfig = new AppConfiguration()
    TagsOperator.apply(appConfig.tagsConfigList)

    it("should evaluate the tags as expected") {
      val tag1 = Tag.newBuilder().setKey("tag_key_3").setVLong(3).build()
      val tag2 = Tag.newBuilder().setKey("tag_key_4").setVLong(1).build()
      val spanTags = Seq(tag1, tag2)
      val resultMap = TagsOperator.eval(spanTags)

      resultMap.size should be(4)
      resultMap("attribute_name_2") should be("4")
    }
  }
}

