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
plugins {
    id("pub.ihub.plugin")
    id("pub.ihub.plugin.ihub-copyright")
    id("pub.ihub.plugin.ihub-git-hooks")
    id("pub.ihub.plugin.ihub-java") apply false
    id("pub.ihub.plugin.ihub-verification") apply false
    id("pub.ihub.plugin.ihub-publish")
}

subprojects {
    !project.pluginManager.hasPlugin("java-platform") || return@subprojects
    apply {
        plugin("pub.ihub.plugin.ihub-java")
        plugin("pub.ihub.plugin.ihub-test")
        plugin("pub.ihub.plugin.ihub-verification")
        plugin("pub.ihub.plugin.ihub-publish")
    }
    dependencies {
        if (project.name != "ihub-core") {
            "api"(project(":ihub-core"))
        }
        if (project.name.endsWith("-spring-boot-starter")) {
            "implementation"("org.springframework.boot:spring-boot-autoconfigure")
            "annotationProcessor"(project(":ihub-process:ihub-process-boot"))
        }
        "annotationProcessor"("org.springframework.boot:spring-boot-autoconfigure-processor")
    }
}

iHubGitHooks {
    hooks.set(mapOf(
        "pre-commit" to "./gradlew build",
        "commit-msg" to "./gradlew commitCheck"
    ))
}
