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
    libs.bundles.platform.get().forEach { api(platform(it)) }
    // 配置组件版本
    constraints {
        api(rootProject)
        libs.bundles.constraints.get().forEach(::api)
    }
}
