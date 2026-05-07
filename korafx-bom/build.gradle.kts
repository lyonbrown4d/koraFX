import org.gradle.api.plugins.JavaPlatformExtension

extensions.configure<JavaPlatformExtension>("javaPlatform") {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":framework-dsl"))
        api(project(":framework-state"))
        api(project(":framework-mvvm"))
        api(project(":framework-navigation"))
        api(project(":framework-theme"))
        api(project(":framework-components"))
    }
}
