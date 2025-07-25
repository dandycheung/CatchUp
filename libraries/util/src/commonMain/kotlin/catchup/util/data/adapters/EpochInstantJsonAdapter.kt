/*
 * Copyright (C) 2019. Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package catchup.util.data.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.Instant

/**
 * Formats dates in UTC seconds or milliseconds time to [Instant] instances.
 *
 * @param durationUnit because some APIs give you UTC time in different units
 */
class EpochInstantJsonAdapter(private val durationUnit: DurationUnit = DurationUnit.SECONDS) :
  JsonAdapter<Instant>() {

  override fun fromJson(reader: JsonReader): Instant? {
    val l = reader.nextLong()
    return Instant.fromEpochMilliseconds(l.seconds.toLong(durationUnit))
  }

  override fun toJson(writer: JsonWriter, value: Instant?) {
    val longTime = value!!.toEpochMilliseconds()
    writer.value(longTime.milliseconds.toLong(durationUnit))
  }
}
