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
    implementation(project(":korafx-navigation"))
    implementation(project(":korafx-components"))
}

application {
    mainClass = "dev.korafx.examples.navigationtheme.MainKt"
}
