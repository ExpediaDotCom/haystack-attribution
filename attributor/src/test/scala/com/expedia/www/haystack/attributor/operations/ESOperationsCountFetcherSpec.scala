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

package com.expedia.www.haystack.attributor.operations

import com.expedia.www.haystack.attributor.config.entities.{ElasticSearchClientConfiguration, ElasticSearchConfiguration}
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

class ESOperationsCountFetcherSpec extends FunSpec with Matchers with EasyMockSugar {
  describe("ESOperationsCountFetcherSpec") {
    val esConfiguration = ElasticSearchConfiguration(
      ElasticSearchClientConfiguration("http://elasticsearch:9200", 1000, 1000),
      "service_metadata"
    )

    val esCountResultJson =
      """
        |{
        |   "count":479,
        |   "_shards":{
        |      "total":4,
        |      "successful":4,
        |      "skipped":0,
        |      "failed":0
        |   }
        |}
      """.stripMargin

    it("should return valid es operations object given a es json result string for count query") {
      val esOperationsCountFetcher = new ESOperationsCountFetcher(esConfiguration)
      val esOperationsCountResult = esOperationsCountFetcher.jsonStringToCountResult(esCountResultJson)
      esOperationsCountResult.count should be(479)
    }

    it("should return valid es endpoint for count query given the configuration") {
      val esOperationsCountFetcher = new ESOperationsCountFetcher(esConfiguration)
      esOperationsCountFetcher.getESCountQueryEndpoint should be("http://elasticsearch:9200/service_metadata/_count")
    }
  }
}