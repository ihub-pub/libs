/*
 * Copyright (c) 2023 the original author or authors.
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
description = "SSO模块授权服务端组件"

dependencies {
    compileOnly("org.springframework.security:spring-security-core")

    runtimeOnly("cn.dev33:sa-token-spring-boot3-starter")

    implementation("cn.hutool:hutool-all")

    // 第三方登录组件
    implementation(libs.justauth)

    // 前端页面静态资源
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.webjars:layui:2.6.8")
    implementation("org.webjars:jquery:3.6.0")
    implementation("org.webjars.npm:crypto-js:4.1.1")
}
