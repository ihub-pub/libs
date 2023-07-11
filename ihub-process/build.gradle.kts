val autoService: Provider<MinimalExternalModuleDependency> = libs.auto.service
val compileTesting: Provider<MinimalExternalModuleDependency> = libs.compile.testing
val incap = libs.incap
val incapProcessor = libs.incap.processor
dependencies {
    // 用于修改AST语法树
    api("com.perfma.wrapped:com.sun.tools:1.8.0_jdk8u275-b01_linux_x64")
    // 用于定义Java源码
    api(libs.javapoet)
    testImplementation(compileTesting)
    testImplementation("org.mockito:mockito-core")
}
subprojects {
    dependencies {
        implementation(project(":ihub-process"))
        compileOnly(autoService)
        compileOnly(incap)
        annotationProcessor(autoService)
        annotationProcessor(incapProcessor)
        testImplementation(compileTesting)
        testImplementation("org.mockito:mockito-core")
    }
}
