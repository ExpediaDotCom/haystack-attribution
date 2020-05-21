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

package com.expedia.www.haystack.attributor.stream.fetcher

import com.expedia.www.haystack.attribution.commons.entities.ServiceStats
import org.apache.kafka.streams.kstream.Windowed
import org.apache.kafka.streams.state.{KeyValueIterator, QueryableStoreTypes}
import org.apache.kafka.streams.{KafkaStreams, KeyValue}

import scala.collection.mutable

/**
  * fetcher returning ServiceStats from current instance
  *
  * @param streams   kafka streams to fetch attributed data
  * @param storeName name of store to read from
  */
class LocalServiceStatsFetcher(streams: KafkaStreams, storeName: String) extends StatsFetcher {

  def fetch(from: Long, to: Long): List[ServiceStats] = {
    val windowedStore = streams.store(storeName, QueryableStoreTypes.windowStore[String, ServiceStats]())
    val iterator: KeyValueIterator[Windowed[String], ServiceStats] = windowedStore.fetchAll(from, to)

    val accumulator = mutable.HashMap[String, ServiceStats]()
    try {
      while (iterator.hasNext) {
        val elem: KeyValue[Windowed[String], ServiceStats] = iterator.next()
        val serviceName = elem.key.key()
        accumulator.get(serviceName) match {
          case Some(stats) => accumulator.update(serviceName, stats + elem.value)
          case _ => accumulator.update(serviceName, elem.value)
        }
      }
    } finally {
      iterator.close()
    }
    accumulator.values.toList
  }
}