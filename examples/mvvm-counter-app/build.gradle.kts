plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.openjfx)
    application
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.graphics")
}

dependencies {
    implementation(project(":korafx-framework"))
    implementation(libs.kotlinx.coroutines.javafx)
}

application {
    mainClass = "dev.korafx.examples.mvvm.MainKt"
}
