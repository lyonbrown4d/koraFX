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
    api(project(":korafx-navigation"))
    api(platform(libs.koin.bom))
    api(libs.koin.core)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.javafx)
    implementation(libs.caffeine)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
