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

package com.expedia.www.haystack.attribution.persistence.email.config.entities

/**
  * Email config for sending the attribution data
  */
trait EmailNotifyConfiguration {
  def fromEmailAddr: String

  def toEmailAddr: List[String] = Nil

  def `type`: String

  def overrideSubjectLine: String
}

/**
  * AWS SES email config
  *
  * @param fromEmailAddr       from address
  * @param toEmailAddr         collection of to address
  * @param `type`              type of email service. email_aws / email_smtp
  * @param overrideSubjectLine override's the default subject line
  * @param awsRegion           aws region name. us-east-1 / us-west-1 etc
  */
case class AwsEmailNotifyConfiguration(override val fromEmailAddr: String, override val toEmailAddr: List[String], override val `type`: String = "email_aws", override val overrideSubjectLine: String, awsRegion: String) extends EmailNotifyConfiguration

/**
  * SMTP email server related config
  *
  * @param fromEmailAddr       from address
  * @param toEmailAddr         collection of to address
  * @param `type`              type of email service. email_aws / email_smtp
  * @param overrideSubjectLine override's the default subject line
  * @param host                hostname of smtp server
  * @param pwd                 password for smtp server
  */
case class SmtpEmailNotifyConfiguration(override val fromEmailAddr: String, override val toEmailAddr: List[String], override val `type`: String = "email_smtp", override val overrideSubjectLine: String, host: String, pwd: String) extends EmailNotifyConfiguration