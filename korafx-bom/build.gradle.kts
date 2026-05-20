import org.gradle.api.plugins.JavaPlatformExtension

extensions.configure<JavaPlatformExtension>("javaPlatform") {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":korafx-dsl"))
        api(project(":korafx-navigation"))
        api(project(":korafx-framework"))
        api(project(":korafx-components"))
        api(project(":korafx-devtools"))
    }
}
