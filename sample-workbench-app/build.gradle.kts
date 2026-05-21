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
    implementation(project(":korafx-components"))
    implementation(project(":korafx-devtools"))
    implementation(project(":korafx-macos"))
    implementation(libs.ikonli.bootstrapicons.pack)
}

application {
    mainClass = "dev.korafx.sample.MainKt"
}
