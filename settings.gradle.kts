/*
 * Copyright (c) 2021-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import pub.ihub.plugin.IHubSettingsExtension

plugins {
    id("pub.ihub.plugin.ihub-settings") version "1.5.0-rc1"
}

configure<IHubSettingsExtension> {
    includeProjects("ihub-core", "ihub-process").skippedDirs("doc").subproject
    includeProjects("ihub-starter").prefix("ihub-boot-").suffix("-spring-boot-starter").onlySubproject
//    includeProjects "ihub-sso" subproject "-spring-boot-starter"
}
