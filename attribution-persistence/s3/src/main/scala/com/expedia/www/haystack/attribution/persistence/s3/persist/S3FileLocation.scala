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

import org.apache.commons.lang3.{StringUtils, Validate}

/**
  * Helper object for getting s3 bucket and folder details for persisting
  */
object S3FileLocation {

  import java.util.{Calendar, TimeZone}

  val UTC = "UTC"
  val FILE_EXTENSION = ".csv"

  def getS3FileMetadata(bucketName: String, basePath: String, timeMs: Long): S3FileMetadata = {
    Validate.notNull(bucketName)
    Validate.notNull(basePath)

    val filePathBuilder: StringBuilder = new StringBuilder

    val previousDay = Calendar.getInstance()
    previousDay.setTimeInMillis(timeMs)
    previousDay.setTimeZone(TimeZone.getTimeZone(UTC))

    if (StringUtils.isNoneEmpty(basePath)) {
      filePathBuilder
        .append(basePath)
        .append("/")
    }

    filePathBuilder
      .append(previousDay.get(Calendar.YEAR))
      .append("/")
      .append(StringUtils.leftPad((previousDay.get(Calendar.MONTH) + 1).toString, 2, "0")) // added 1 since Calender month starts with 0 (January)
      .append("/")
      .append(StringUtils.leftPad(previousDay.get(Calendar.DAY_OF_MONTH).toString, 2, "0"))
      .append(FILE_EXTENSION)

    S3FileMetadata(bucketName, filePathBuilder.toString)
  }

  /**
    * S3 details of bucket name and file path for persisting
    *
    * @param bucketName s3 bucket name
    * @param filePath   s3 file path in the above bucket
    */
  case class S3FileMetadata(bucketName: String, filePath: String)
}