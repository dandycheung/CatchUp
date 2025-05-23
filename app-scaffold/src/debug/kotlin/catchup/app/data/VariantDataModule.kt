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
package catchup.app.data

import catchup.util.injection.qualifiers.NetworkInterceptor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import timber.log.Timber

private inline fun httpLoggingInterceptor(
  level: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.NONE,
  crossinline logger: (String) -> Unit,
): HttpLoggingInterceptor {
  return HttpLoggingInterceptor { message -> logger(message) }.also { it.level = level }
}

@ContributesTo(AppScope::class)
interface VariantDataModule {

  @SingleIn(AppScope::class)
  @Provides
  @NetworkInterceptor
  @IntoSet
  fun provideLoggingInterceptor(): Interceptor =
    httpLoggingInterceptor(BASIC) { message -> Timber.tag("OkHttp").v(message) }
}
