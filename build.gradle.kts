/*
 * Copyright (c) 2021 Henry 李恒 (henry.box@outlook.com).
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
plugins {
    `version-catalog`
    id("pub.ihub.plugin")
    id("pub.ihub.plugin.ihub-copyright")
    id("pub.ihub.plugin.ihub-git-hooks")
    id("pub.ihub.plugin.ihub-java") apply false
    id("pub.ihub.plugin.ihub-verification") apply false
    id("pub.ihub.plugin.ihub-publish")
}

subprojects {
    if (listOf("ihub-bom", "ihub-dependencies").contains(project.name)) {
        return@subprojects
    }
    apply {
        plugin("pub.ihub.plugin.ihub-java")
        plugin("pub.ihub.plugin.ihub-test")
        plugin("pub.ihub.plugin.ihub-verification")
        plugin("pub.ihub.plugin.ihub-publish")
    }
    iHubBom.bomVersions.clear()
    dependencies {
        platform(project(":ihub-dependencies")).let {
            "implementation"(it)
            "pmd"(it)
            "annotationProcessor"(it)
            "testAnnotationProcessor"(it)
        }
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

catalog {
    versionCatalog {
        from(files("gradle/libs.versions.toml"))
    }
}
