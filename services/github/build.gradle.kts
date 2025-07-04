/*
 * Copyright (c) 2018 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
  alias(libs.plugins.foundry.base)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.apollo)
}

android {
  defaultConfig {
    buildConfigField("String", "GITHUB_DEVELOPER_TOKEN",
        "\"${properties["catchup_github_developer_token"]}\"")
  }
  buildFeatures {
    buildConfig = true
  }
  namespace = "catchup.service.github"
}

apollo {
  service("github") {
    mapScalar("DateTime", "kotlin.time.Instant")
    mapScalar("URI", "okhttp3.HttpUrl")
    packageName.set("catchup.service.github")
    schemaFiles.from(file("src/main/graphql/catchup/service/github/schema.json"))
  }
}

foundry {
  features {
    metro()
  }
  android {
    features {
      resources("catchup_service_github_")
    }
  }
}

dependencies {
 api(project(":libraries:appconfig"))
 api(project(":libraries:di"))
 api(project(":libraries:gemoji"))
 api(project(":service-api"))
 api(libs.apollo.api)
 api(libs.apollo.runtime)
 api(libs.kotlin.datetime)
 api(libs.okhttp.core)
 api(libs.retrofit.core)

 implementation(project(":libraries:retrofitconverters"))
 implementation(project(":libraries:util"))
 implementation(libs.apollo.httpcache)
 // Apollo
 implementation(libs.apollo.runtime)
 implementation(libs.kotlin.datetime)
 implementation(libs.misc.jsoup)
 implementation(libs.misc.timber)
 implementation(libs.okhttp.core)
}
