pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "korafx"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    ":korafx-bom",
    ":korafx-dsl",
    ":korafx-framework",
    ":korafx-components",
    ":examples:dsl-basic-app",
    ":examples:mvvm-counter-app",
    ":examples:navigation-theme-app",
    ":sample-workbench-app",
)
