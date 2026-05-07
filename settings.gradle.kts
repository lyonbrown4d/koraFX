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
    ":framework-dsl",
    ":framework-state",
    ":framework-mvvm",
    ":framework-navigation",
    ":framework-theme",
    ":framework-components",
    ":sample-workbench-app",
)
