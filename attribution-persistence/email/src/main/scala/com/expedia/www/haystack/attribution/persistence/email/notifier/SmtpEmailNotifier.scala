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

import java.util.Date

import com.expedia.www.haystack.attribution.commons.entities.ServiceStats
import com.expedia.www.haystack.attribution.persistence.email.config.entities.SmtpEmailNotifyConfiguration
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

/**
  * Notifier class for smtp service
  *
  * @param emailConfig   smtp email config
  * @param envName       name of the env like dev, test or any string from config
  * @param emailTemplate html template of email body
  */
class SmtpEmailNotifier(emailConfig: SmtpEmailNotifyConfiguration, envName: String, emailTemplate: String) extends StatsNotifier {
  private val LOGGER = LoggerFactory.getLogger(classOf[SmtpEmailNotifier])

  def send(stats: List[ServiceStats]): Unit = {

    val props = System.getProperties
    props.put("mail.smtp.host", emailConfig.host)

    var auth: Authenticator = null
    if (!StringUtils.isBlank(emailConfig.pwd)) { // check if authentication is required
      auth = new Authenticator {
        override def getPasswordAuthentication: PasswordAuthentication = new PasswordAuthentication(emailConfig.fromEmailAddr, emailConfig.pwd)
      }
    }

    val session = Session.getInstance(props, auth)
    val msg = new MimeMessage(session)

    msg.addHeader("Content-type", "text/HTML; charset=UTF-8")
    msg.addHeader("format", "flowed")
    msg.addHeader("Content-Transfer-Encoding", "8bit")

    msg.setFrom(emailConfig.fromEmailAddr)
    msg.setReplyTo(emailConfig.toEmailAddr.map(new InternetAddress(_)).toArray)
    msg.setSubject(emailConfig.overrideSubjectLine, "UTF-8")
    msg.setText(EmailUtils.getEmailBody(emailTemplate, stats), "UTF-8")

    msg.setSentDate(new Date)

    val addresses: Array[Address] = emailConfig.toEmailAddr.map(emailAdd => new InternetAddress(emailAdd)).toArray
    msg.setRecipients(Message.RecipientType.TO, addresses)
    Transport.send(msg)

    LOGGER.info(s"Email is sent successfully. Message - $msg")
  }

  override val `type`: String = "email_smtp"
}