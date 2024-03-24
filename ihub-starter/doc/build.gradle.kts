/*
 * Copyright (c) 2024 the original author or authors.
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

description = "文档模块组件自动配置"

iHubJava {
    registerFeature("servlet", "web-support", "servlet-support")
    registerFeature("reactor", "web-support", "reactor-support")
}

dependencies {
    implementation("pub.ihub.integration:ihub-core")
    annotationProcessor("pub.ihub.integration:ihub-process-core")

    implementation("org.springdoc:springdoc-openapi-starter-common")
    "servletImplementation"("org.springdoc:springdoc-openapi-starter-webmvc-api")
    "reactorImplementation"("org.springdoc:springdoc-openapi-starter-webflux-api")

    testImplementation(project(":ihub-boot-test-spring-boot-starter"))
    testImplementation("org.springframework.boot:spring-boot-starter-web")
}
