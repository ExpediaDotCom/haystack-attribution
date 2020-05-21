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

package com.expedia.www.haystack.attribution.persistence.s3.config

import com.expedia.www.haystack.attribution.persistence.s3.config.entities._
import com.expedia.www.haystack.attribution.persistence.s3.transformer.ServiceStatsTransformer
import com.expedia.www.haystack.commons.config.ConfigurationLoader
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.config.ConfigRenderOptions
import org.slf4j.LoggerFactory

import scala.reflect.ClassTag

/**
  * This class reads the configuration from the given resource name using {@link ConfigurationLoader ConfigurationLoader}
  */
class AppConfiguration() {
  private val LOGGER = LoggerFactory.getLogger(classOf[AppConfiguration])
  private val config = ConfigurationLoader.loadConfigFileWithEnvOverrides()

  LOGGER.info(config.root()
    .render(ConfigRenderOptions.defaults().setOriginComments(false))
    .replaceAll("(?i)(\\\".*password\\\"\\s*:\\s*)\\\".+\\\"", "$1********"))

  lazy val persistS3Configs: PersistS3ConfigurationList = {
    val jsonString = config.getString("persist.s3")
    val objectMapper = new ObjectMapper with ScalaObjectMapper
    objectMapper.registerModule(DefaultScalaModule)
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.setSerializationInclusion(Include.NON_NULL)

    objectMapper.readValue(jsonString, classOf[PersistS3ConfigurationList])
  }

  lazy val attributorConfig: AttributorConfiguration = {
    val cfg = config.getConfig("attributor.client")
    AttributorConfiguration(
      cfg.getString("scheme"),
      cfg.getString("host"),
      cfg.getString("path"),
      cfg.getInt("port"),
      cfg.getInt("conn.timeout.ms"),
      cfg.getInt("read.timeout.ms"))
  }

  lazy val transformers: List[ServiceStatsTransformer] = {
    transformersConfig.items.map(transformerConfig => {
      val transformer = toInstance[ServiceStatsTransformer](transformerConfig.classRelativePath)
      transformer.init(transformerConfig.id, transformerConfig.customTags, config)
      transformer
    })
  }

  val transformersConfig: TransformerConfigurationList = {
    if (config.hasPath("transformers.config")) {
      val jsonString = config.getString("transformers.config")
      val objectMapper = new ObjectMapper with ScalaObjectMapper
      objectMapper.registerModule(DefaultScalaModule)
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      objectMapper.setSerializationInclusion(Include.NON_NULL)

      objectMapper.readValue(jsonString, classOf[TransformerConfigurationList])
    } else {
      TransformerConfigurationList(List())
    }
  }

  private def toInstance[T](className: String)(implicit ct: ClassTag[T]): T = {
    val c = Class.forName(className)
    if (c == null) {
      throw new RuntimeException(s"No class found with name $className")
    } else {
      val o = c.newInstance()
      val baseClass = ct.runtimeClass

      if (!baseClass.isInstance(o)) {
        throw new RuntimeException(s"${c.getName} is not an instance of ${baseClass.getName}")
      }
      o.asInstanceOf[T]
    }
  }

  val environmentName: String = config.getString("environment.name")
}