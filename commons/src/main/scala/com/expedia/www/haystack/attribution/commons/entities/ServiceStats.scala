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

package com.expedia.www.haystack.attribution.commons.entities

import java.util
import java.util.Date

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.attribution.commons.entities.ValueType.ValueType
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization

import scala.collection.JavaConverters._

/**
  * Companion object for ServiceStats providing serde implementation with some other helper methods
  */
object ServiceStats {
  implicit val formats = DefaultFormats

  def serde(): Serde[ServiceStats] = {
    new Serde[ServiceStats] {
      override def deserializer(): Deserializer[ServiceStats] = new Deserializer[ServiceStats] {
        override def configure(map: util.Map[String, _], b: Boolean): Unit = ()

        override def close(): Unit = ()

        override def deserialize(s: String, bytes: Array[Byte]): ServiceStats = Serialization.read[ServiceStats](new String(bytes))
      }

      override def serializer(): Serializer[ServiceStats] = new Serializer[ServiceStats]() {
        override def configure(map: util.Map[String, _], b: Boolean): Unit = ()

        override def serialize(s: String, t: ServiceStats): Array[Byte] = Serialization.write(t).getBytes("utf-8")

        override def close(): Unit = ()
      }

      override def configure(map: util.Map[String, _], b: Boolean): Unit = ()

      override def close(): Unit = ()
    }
  }

  def getStatsTags(span: Span): Map[String, AttributedTagValue] = {
    var statsTags = Map[String, AttributedTagValue]()
    statsTags ++= TagsOperator.eval(span.getTagsList.asScala)
    statsTags
  }
}

/**
  * Aggregated stats for a service. Also lets to aggregate multiple stats
  *
  * @param serviceName    service name
  * @param spanCount      count of span
  * @param spanSizeBytes  size of spans in bytes
  * @param vNodeId        combination of operation name and a number to make it random for distribution
  * @param lastSeen       time when it is processed
  * @param attributedTags map containing aggregated value of tags provided via configuration
  * @param operationCount count of operationNames for this service
  */
case class ServiceStats(serviceName: String,
                        spanCount: Long = 0,
                        spanSizeBytes: Long = 0,
                        vNodeId: Int = 0,
                        lastSeen: Date = new Date(),
                        attributedTags: Map[String, AttributedTagValue] = Map[String, AttributedTagValue](),
                        var operationCount: Long = 0) {

  def +(other: ServiceStats): ServiceStats = {
    ServiceStats(
      serviceName,
      spanCount = spanCount + other.spanCount,
      spanSizeBytes = spanSizeBytes + other.spanSizeBytes,
      vNodeId,
      attributedTags = TagsOperator.aggregateTags(this, other).toMap
    )
  }

  def updateOperationCount(operationCount: Long): Unit = {
    this.operationCount = operationCount
  }
}

case class AttributedTagValue(tagValue: String = "",
                              valueType: String = "") {

  def +(other: AttributedTagValue): AttributedTagValue = {
    AttributedTagValue((getTagValue + other.getTagValue).toString,
      valueType)
  }

  def +(other: Option[AttributedTagValue]): AttributedTagValue = {
    AttributedTagValue((getTagValue + other.getOrElse(AttributedTagValue("", valueType)).getTagValue).toString,
      valueType)
  }

  def incrementValue: AttributedTagValue = {
    AttributedTagValue((getTagValue + 1).toString, valueType)
  }

  def getTagValue: Double = {
    try {
      tagValue.toDouble
    } catch {
      case _: Throwable => 0
    }
  }
}

object ValueType extends Enumeration {
  type ValueType = Value
  val COUNT: ValueType.Value = Value("COUNT")
  val BYTES: ValueType.Value = Value("BYTES")
  val NONE: ValueType.Value = Value("NONE")
}