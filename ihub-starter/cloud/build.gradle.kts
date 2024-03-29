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

description = "spring cloud自动配置"

iHubJava {
    // Web Support
    registerFeature("servlet", "web-support", "servlet-support")
    registerFeature("reactor", "web-support", "reactor-support")

    // Monitor Support
    registerFeature("admin", "monitor-support", "admin-support")

    // Sleuth Support
    registerFeature("zipkin", "sleuth-support", "zipkin-support")

    // Eureka Support
    registerFeature("eureka", "discovery-support", "eureka-support")
    // Zookeeper Support
    registerFeature("zookeeper", "discovery-support", "config-support", "zookeeper-support")
    // Consul Support （暂不适配）
    registerFeature("consul", "discovery-support", "config-support", "bus-support", "consul-support")
    // Nacos Support
    registerFeature("nacos", "discovery-support", "config-support", "nacos-support")
    // Spring Cloud Config Support （暂不适配）
    registerFeature("springConfig", "config-support", "springConfig-support")
    // Apollo Support （暂不适配）
    registerFeature("apollo", "config-support", "apollo-support")

    // Breaker Support
    registerFeature("sentinel", "breaker-support", "sentinel-support")
    // Container Support
    registerFeature("tomcat", "container-support", "tomcat-support")
    registerFeature("jetty", "container-support", "jetty-support")
    registerFeature("undertow", "container-support", "undertow-support")
}

dependencies {
    "servletApi"("org.springframework.boot:spring-boot-starter-web") {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
    "tomcatRuntimeOnly"("org.springframework.boot:spring-boot-starter-tomcat")
//    "jettyRuntimeOnly"("org.springframework.boot:spring-boot-starter-jetty")
//    "undertowRuntimeOnly"("org.springframework.boot:spring-boot-starter-undertow")
    "reactorApi"("org.springframework.boot:spring-boot-starter-webflux")

    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.cloud:spring-cloud-starter-bootstrap")

    "adminApi"("de.codecentric:spring-boot-admin-starter-client")

//    zipkinApi "org.springframework.cloud:spring-cloud-starter-sleuth",
//        "org.springframework.cloud:spring-cloud-sleuth-zipkin"

    "eurekaApi"("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    "nacosApi"("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
    "nacosApi"("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config")
    "nacosApi"("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    "sentinelApi"("com.alibaba.cloud:spring-cloud-starter-alibaba-sentinel")

    "zookeeperApi"("org.springframework.cloud:spring-cloud-starter-zookeeper-all")

    testImplementation(project(":ihub-boot-test-spring-boot-starter"))
}
