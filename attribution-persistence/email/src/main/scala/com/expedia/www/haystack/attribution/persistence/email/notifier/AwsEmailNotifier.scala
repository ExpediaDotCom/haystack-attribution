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

package com.expedia.www.haystack.attribution.persistence.email.notifier

import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model._
import com.expedia.www.haystack.attribution.commons.entities.ServiceStats
import com.expedia.www.haystack.attribution.persistence.email.config.entities.AwsEmailNotifyConfiguration
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * Notifier class for AWS SES service
  *
  * @param emailConfig   AWS SES config
  * @param envName       name of the env like dev, test or any string from config
  * @param emailTemplate html template of email body
  */
class AwsEmailNotifier(emailConfig: AwsEmailNotifyConfiguration, envName: String, emailTemplate: String) extends StatsNotifier {
  private val LOGGER = LoggerFactory.getLogger(classOf[AwsEmailNotifier])
  private lazy val sesClient = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.fromName(emailConfig.awsRegion)).build()

  def send(stats: List[ServiceStats]): Unit = {
    try {
      val emailBody = EmailUtils.getEmailBody(emailTemplate, stats)

      val request = new SendEmailRequest()
        .withDestination(
          new Destination().withToAddresses(emailConfig.toEmailAddr.asJava))
        .withMessage(new Message()
          .withBody(new Body()
            .withHtml(new Content()
              .withCharset("UTF-8").withData(emailBody)))
          .withSubject(new Content()
            .withCharset("UTF-8").withData(emailConfig.overrideSubjectLine)))
        .withSource(emailConfig.fromEmailAddr)

      val result = sesClient.sendEmail(request)
      LOGGER.info(s"Email is sent successfully. Send result - ${result.toString}")
    } catch {
      case ex: Exception => LOGGER.error("Fail to send the email with error", ex)
    }
  }

  override val `type`: String = "email_aws"
}