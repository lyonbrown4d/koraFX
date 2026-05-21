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
    ":korafx-navigation",
    ":korafx-framework",
    ":korafx-command-palette",
    ":korafx-components",
    ":korafx-data-grid",
    ":korafx-inspector-panel",
    ":korafx-resource-explorer",
    ":korafx-virtual-list",
    ":korafx-source-editor",
    ":korafx-graph-editor",
    ":korafx-test",
    ":korafx-devtools",
    ":korafx-macos",
    ":examples:dsl-basic-app",
    ":examples:mvvm-counter-app",
    ":examples:navigation-theme-app",
    ":sample-workbench-app",
)
