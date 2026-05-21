import org.gradle.api.plugins.JavaPlatformExtension

extensions.configure<JavaPlatformExtension>("javaPlatform") {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":korafx-dsl"))
        api(project(":korafx-navigation"))
        api(project(":korafx-framework"))
        api(project(":korafx-command-palette"))
        api(project(":korafx-components"))
        api(project(":korafx-data-grid"))
        api(project(":korafx-inspector-panel"))
        api(project(":korafx-resource-explorer"))
        api(project(":korafx-source-editor"))
        api(project(":korafx-workspace"))
        api(project(":korafx-test"))
        api(project(":korafx-devtools"))
        api(project(":korafx-macos"))
    }
}
