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
    ":framework-dsl",
    ":framework-state",
    ":framework-mvvm",
    ":framework-navigation",
    ":framework-theme",
    ":framework-components",
    ":examples:dsl-basic-app",
    ":examples:mvvm-counter-app",
    ":examples:navigation-theme-app",
    ":sample-workbench-app",
)
