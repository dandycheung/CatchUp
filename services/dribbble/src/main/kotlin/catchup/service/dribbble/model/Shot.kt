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
package catchup.service.dribbble.model

import kotlin.time.Instant

/** Models a dibbble shot */
data class Shot(
  val commentsCount: Long,
  val createdAt: Instant,
  val description: String?,
  val htmlUrl: String,
  val id: Long,
  val title: String,
  val imageUrl: String,
  val imageAlt: String,
  val videoUrl: String?,
  val likesCount: Long,
  val user: User,
  val viewsCount: Long,
)
