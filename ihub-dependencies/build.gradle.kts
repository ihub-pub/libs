description = "组件依赖版本管理"

plugins {
    `java-platform`
}

apply {
    plugin("pub.ihub.plugin.ihub-publish")
}

javaPlatform {
    allowDependencies()
}

iHubBom.bomVersions.clear()

dependencies {
    // 导入bom配置（优先级由上至下）
    api(platform(project(":ihub-bom")))
//    api(platform("pub.ihub.keel:ihub-bom:main-SNAPSHOT"))
    api(platform("org.jmolecules:jmolecules-bom:2022.3.0"))
//    api(platform("org.springframework.experimental:spring-modulith-bom:0.1.0"))
    api(platform("cn.hutool:hutool-bom:5.8.20"))
    api(platform("org.spockframework:spock-bom:2.3-groovy-4.0"))
    api(platform("cn.dev33:sa-token-bom:1.35.0.RC"))
    api(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.3"))
    api(platform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2021.1"))
    api(platform("de.codecentric:spring-boot-admin-dependencies:3.1.1"))
    api(platform("com.google.guava:guava-bom:32.1.1-jre"))
    api(platform("com.google.inject:guice-bom:7.0.0"))
    api(platform("org.axonframework:axon-bom:4.8.0"))
    api(platform("org.springframework.boot:spring-boot-dependencies:3.1.1"))
    api(platform("io.quarkus:quarkus-bom:3.2.0.Final"))
    api(platform("org.springdoc:springdoc-openapi:2.1.0"))
    // 配置组件版本
    constraints {
        api(rootProject)
        api("com.alibaba.fastjson2:fastjson2:2.0.35")
        api("com.alibaba.p3c:p3c-pmd:2.1.1")
        api("com.baomidou:mybatis-plus:${libs.versions.mybatis.plus.get()}")
        api("com.baomidou:mybatis-plus-annotation:${libs.versions.mybatis.plus.get()}")
        api("com.baomidou:mybatis-plus-core:${libs.versions.mybatis.plus.get()}")
        api("com.baomidou:mybatis-plus-extension:${libs.versions.mybatis.plus.get()}")
        api("com.baomidou:mybatis-plus-boot-starter:${libs.versions.mybatis.plus.get()}")
        api("com.baomidou:mybatis-plus-generator:${libs.versions.mybatis.plus.get()}")
        api("com.athaydes:spock-reports:2.5.0-groovy-4.0")
        api("com.google.auto.service:auto-service:1.1.1")
        api("com.google.testing.compile:compile-testing:0.21.0")
        api("net.ltgt.gradle.incap:incap:1.0.0")
        api("net.ltgt.gradle.incap:incap-processor:1.0.0")
        api("com.squareup:javapoet:1.13.0")
        api("org.mapstruct:mapstruct:${libs.versions.mapstruct.get()}")
        api("org.mapstruct:mapstruct-processor:${libs.versions.mapstruct.get()}")
        api("me.zhyd.oauth:JustAuth:1.16.5")
    }
}
