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

package com.expedia.www.haystack.attribution.persistence.email.config

import com.expedia.www.haystack.attribution.commons.config.{TagsConfig, TagsConfigList}
import com.expedia.www.haystack.attribution.persistence.email.config.entities.{AttributorConfiguration, AwsEmailNotifyConfiguration, EmailNotifyConfiguration, SmtpEmailNotifyConfiguration}
import com.expedia.www.haystack.commons.config.ConfigurationLoader
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.config.ConfigRenderOptions
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

/**
  * This class reads the configuration from the given resource name using {@link ConfigurationLoader ConfigurationLoader}
  */
class AppConfiguration() {
  private val LOGGER = LoggerFactory.getLogger(classOf[AppConfiguration])
  private val config = ConfigurationLoader.loadConfigFileWithEnvOverrides()
  private final val defaultEmailSubject = "[Haystack Attribution] Daily Usage Report"

  LOGGER.info(config.root()
    .render(ConfigRenderOptions.defaults().setOriginComments(false))
    .replaceAll("(?i)(\\\".*password\\\"\\s*:\\s*)\\\".+\\\"", "$1********"))

  lazy val emailNotifyConfig: EmailNotifyConfiguration = {
    val cfg = config.getConfig("notify.email")
    val emailType = config.getString("notify.type")
    val subjectLine = {
      if (cfg.hasPath("override.subject.line") && cfg.getString("override.subject.line").nonEmpty)
        cfg.getString("override.subject.line")
      else
        defaultEmailSubject
    }

    if (emailType.equalsIgnoreCase("email_aws"))
      AwsEmailNotifyConfiguration(cfg.getString("from"),
        cfg.getString("to").split(",").toList,
        emailType,
        subjectLine,
        cfg.getString("aws.region"))
    else
      SmtpEmailNotifyConfiguration(cfg.getString("from"),
        cfg.getString("to").split(",").toList,
        emailType,
        subjectLine,
        cfg.getString("smpt.host"),
        cfg.getString("smpt.pwd"))
  }

  lazy val attributorConfig: AttributorConfiguration = {
    val cfg = config.getConfig("attributor.client")
    AttributorConfiguration(cfg.getString("host"),
      cfg.getString("path"),
      cfg.getInt("port"),
      cfg.getInt("conn.timeout.ms"),
      cfg.getInt("read.timeout.ms"))
  }

  lazy val tagsConfigList: Seq[TagsConfig] = {
    if (config.hasPath("tags.json") && StringUtils.isNoneEmpty(config.getString("tags.json"))) {
      val jsonString = config.getString("tags.json")
      val objectMapper = new ObjectMapper with ScalaObjectMapper
      objectMapper.registerModule(DefaultScalaModule)
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      objectMapper.setSerializationInclusion(Include.NON_NULL)

      val tags: TagsConfigList = objectMapper.readValue(jsonString, classOf[TagsConfigList])
      tags.items
    } else {
      Seq()
    }
  }

  val emailTemplate: String = {
    if (config.hasPath("notify.email.override.template") && config.getString("notify.email.override.template").nonEmpty) {
      config.getString("notify.email.override.template")
    } else {
      scala.io.Source.fromInputStream(this.getClass.getResourceAsStream("/stats_email.tmpl")).getLines().mkString("\n")
    }
  }

  val environmentName: String = config.getString("environment.name")
}