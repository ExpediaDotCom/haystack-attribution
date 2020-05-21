package com.expedia.www.haystack.attribution.commons

import com.expedia.www.haystack.attribution.commons.entities.{AttributedTagValue, ValueType}

object Utils {
  val TOTAL = "Total"

  private val KB_IN_BYTES = 1024.0
  private val MB_IN_BYTES = 1024.0 * KB_IN_BYTES
  private val GB_IN_BYTES = 1024.0 * MB_IN_BYTES
  private val TB_IN_BYTES = 1024.0 * GB_IN_BYTES

  private val THOUSAND = 1000.0
  private val MILLION = THOUSAND * THOUSAND
  private val BILLION = MILLION * THOUSAND
  private val COUNT_FORMAT_MAP: Seq[(Double, String)] = Seq((BILLION, "Bi"), (MILLION, "Mi"), (THOUSAND, "K"))
  private val SIZE_FORMAT_MAP: Seq[(Double, String)] = Seq((TB_IN_BYTES, "tb"), (GB_IN_BYTES, "gb"), (MB_IN_BYTES, "mb"), (KB_IN_BYTES, "kb"))

  private def format(formatMap: Seq[(Double, String)])(rawValue: String): String = {

    val parsedValue = try {
      rawValue.toDouble
    } catch {
      case _: Throwable => 0
    }

    val formatted = formatMap.find {
      case (num, _) => Math.floor(parsedValue / num) != 0.0
    } map {
      case (num, unit: String) => f"${parsedValue / num}%1.1f$unit"
    }

    formatted.getOrElse(parsedValue.toLong).toString
  }

  def format(value: String, valueType: String): String = {
    ValueType.withName(valueType.toUpperCase) match {
      case ValueType.COUNT => format(COUNT_FORMAT_MAP)(value)
      case ValueType.BYTES => format(SIZE_FORMAT_MAP)(value)
      case _ => value
    }
  }

  def format(attributedTagValue: AttributedTagValue): String = {
    format(attributedTagValue.tagValue, attributedTagValue.valueType)
  }

  def is2xx(status: Int): Boolean = (status / 100) == 2

  def hourToMs(hours: Int): Long = hours * 60 * 60 * 1000
}