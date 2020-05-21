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

import com.amazonaws.auth.profile.internal.securitytoken.{RoleInfo, STSProfileCredentialsServiceProvider}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import org.slf4j.LoggerFactory

/**
  * S3 writer for persisting string in AWS s3
  *
  * @param s3Bucket   s3 bucket name
  * @param useStsRole enable/disable aws cross account sts role
  * @param stsRoleArn sts role arn for writing cross account
  */
class S3Writer(s3Bucket: String, useStsRole: Boolean, stsRoleArn: String) {
  private val LOGGER = LoggerFactory.getLogger(classOf[S3Writer])

  def send(csvString: String, filePath: String): Unit = {
    try {
      val s3Client: AmazonS3 = getS3Client(useStsRole, stsRoleArn)
      s3Client.putObject(s3Bucket, filePath, csvString)
      LOGGER.info(s"Successfully written the file $filePath to $s3Bucket")
    }
    catch {
      case ex: Exception => LOGGER.error(s"Failed to write to $stsRoleArn with error. Report: $csvString", ex)
    }
  }

  private def getS3Client(useStsRole: Boolean, stsRoleArn: String): AmazonS3 = {
    if (useStsRole) {
      AmazonS3ClientBuilder.standard()
        .withCredentials(new STSProfileCredentialsServiceProvider(new RoleInfo().withRoleArn(stsRoleArn).withRoleSessionName("haystack-attribution-s3-session")))
        .build()
    } else {
      AmazonS3ClientBuilder.standard()
        .build()
    }
  }
}