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
    implementation(project(":korafx-command-palette"))
    implementation(project(":korafx-components"))
    implementation(project(":korafx-data-grid"))
    implementation(project(":korafx-graph-editor"))
    implementation(project(":korafx-inspector-panel"))
    implementation(project(":korafx-virtual-list"))
    implementation(project(":korafx-resource-explorer"))
    implementation(project(":korafx-source-editor"))
    implementation(project(":korafx-workspace"))
    implementation(project(":korafx-devtools"))
    implementation(project(":korafx-macos"))
    implementation(libs.ikonli.bootstrapicons.pack)
}

application {
    mainClass = "dev.korafx.sample.MainKt"
}
