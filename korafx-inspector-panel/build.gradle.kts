plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.openjfx)
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.graphics")
}

dependencies {
    api(project(":korafx-components"))
    testImplementation(project(":korafx-test"))
    testImplementation(libs.kotlin.test.junit5)
}

tasks.test {
    useJUnitPlatform()
}
