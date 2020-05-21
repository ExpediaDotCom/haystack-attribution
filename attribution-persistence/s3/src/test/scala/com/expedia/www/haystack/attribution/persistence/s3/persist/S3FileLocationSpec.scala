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

package com.expedia.www.haystack.attribution.persistence.s3.persist

import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

class S3FileLocationSpec extends FunSpec with Matchers with EasyMockSugar {
  describe("S3FileLocationSpec") {
    it("should pad 0 appropriately to month and return correct path for given s3 location") {
      val timeInMs = 1547639526000l //GMT: Wednesday, January 16, 2019 11:52:06 AM
      val s3FileLocation = S3FileLocation.getS3FileMetadata("haystack-bucket", "attribution", timeInMs)

      s3FileLocation.bucketName should be("haystack-bucket")
      s3FileLocation.filePath should be("attribution/2019/01/16.csv")
    }

    it("should pad 0 appropriately to day and return correct path for given s3 location") {
      val timeInMs = 1547034726000l //GMT: Wednesday, January 9, 2019 11:52:06 AM
      val s3FileLocation = S3FileLocation.getS3FileMetadata("haystack-bucket", "attribution", timeInMs)

      s3FileLocation.bucketName should be("haystack-bucket")
      s3FileLocation.filePath should be("attribution/2019/01/09.csv")
    }

    it("shouldn't pad 0 and return correct path for given s3 location") {
      val timeInMs = 1570881126000l //GMT: Saturday, October 12, 2019 11:52:06 AM
      val s3FileLocation = S3FileLocation.getS3FileMetadata("haystack-bucket", "attribution", timeInMs)

      s3FileLocation.bucketName should be("haystack-bucket")
      s3FileLocation.filePath should be("attribution/2019/10/12.csv")
    }
  }
}