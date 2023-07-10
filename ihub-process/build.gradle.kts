dependencies {
    // 用于修改AST语法树
    api("com.perfma.wrapped:com.sun.tools:1.8.0_jdk8u275-b01_linux_x64")
    // 用于定义Java源码
    api("com.squareup:javapoet")
    testImplementation("com.google.testing.compile:compile-testing")
    testImplementation("org.mockito:mockito-core")
}
subprojects {
    dependencies {
        implementation(project(":ihub-process"))
        compileOnly("com.google.auto.service:auto-service")
        compileOnly("net.ltgt.gradle.incap:incap")
        annotationProcessor("com.google.auto.service:auto-service")
        annotationProcessor("net.ltgt.gradle.incap:incap-processor")
        testImplementation("com.google.testing.compile:compile-testing")
        testImplementation("org.mockito:mockito-core")
    }
}
