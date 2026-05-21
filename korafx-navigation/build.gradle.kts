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
    api(project(":korafx-dsl"))
    implementation(project(":korafx-components"))
    api(libs.ikonli.javafx)
    api(libs.kotlinx.coroutines.core)
    testImplementation(project(":korafx-test"))
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ikonli.bootstrapicons.pack)
}

tasks.test {
    useJUnitPlatform()
}
