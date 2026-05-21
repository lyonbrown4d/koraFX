import org.gradle.api.Project
import org.gradle.api.provider.Provider

private val publishableModules =
    setOf(
        "korafx-bom",
        "korafx-framework",
        "korafx-command-palette",
        "korafx-navigation",
        "korafx-dsl",
        "korafx-components",
        "korafx-data-grid",
        "korafx-inspector-panel",
        "korafx-resource-explorer",
        "korafx-source-editor",
        "korafx-workspace",
        "korafx-test",
        "korafx-devtools",
        "korafx-macos",
    )

private val publishingPropertyKeys =
    listOf(
        "mavenCentralUsername",
        "mavenCentralPassword",
        "signingInMemoryKey",
        "signingInMemoryKeyId",
        "signingInMemoryKeyPassword",
        "POM_NAME",
        "POM_DESCRIPTION",
        "POM_INCEPTION_YEAR",
        "POM_URL",
        "POM_LICENSE_NAME",
        "POM_LICENSE_URL",
        "POM_LICENSE_DIST",
        "POM_DEVELOPER_ID",
        "POM_DEVELOPER_NAME",
        "POM_DEVELOPER_URL",
        "POM_SCM_URL",
        "POM_SCM_CONNECTION",
        "POM_SCM_DEV_CONNECTION",
    )

fun Project.isPublishableLeafModule(): Boolean = name in publishableModules && childProjects.isEmpty()

fun Project.isBomModule(): Boolean = name == "korafx-bom"

fun Project.publishedArtifactId(): String =
    when (name) {
        "korafx-bom" -> "korafx-bom"
        "korafx-framework" -> "korafx-framework"
        "korafx-command-palette" -> "korafx-command-palette"
        "korafx-navigation" -> "korafx-navigation"
        "korafx-dsl" -> "korafx-dsl"
        "korafx-components" -> "korafx-components"
        "korafx-data-grid" -> "korafx-data-grid"
        "korafx-inspector-panel" -> "korafx-inspector-panel"
        "korafx-resource-explorer" -> "korafx-resource-explorer"
        "korafx-source-editor" -> "korafx-source-editor"
        "korafx-workspace" -> "korafx-workspace"
        "korafx-test" -> "korafx-test"
        "korafx-devtools" -> "korafx-devtools"
        "korafx-macos" -> "korafx-macos"
        else -> name
    }

fun Project.publishedDescription(): String =
    when (name) {
        "korafx-bom" -> "Bill of materials for aligning KoraFX module versions."
        "korafx-command-palette" -> "Command palette surfaces and command host primitives for KoraFX applications."
        "korafx-navigation" -> "Navigation primitives and route-aware UI for KoraFX JavaFX applications."
        "korafx-dsl" -> "Kotlin-first JavaFX DSL and Flow state binding primitives."
        "korafx-framework" -> "Kotlin-first JavaFX application framework with Koin, MVVM, navigation and theme services."
        "korafx-components" -> "Reusable JavaFX workbench components for KoraFX applications."
        "korafx-data-grid" -> "Data grid and editable table surfaces for KoraFX applications."
        "korafx-inspector-panel" -> "Inspector panel and property detail surfaces for KoraFX applications."
        "korafx-resource-explorer" -> "Resource tree explorer surfaces for KoraFX applications."
        "korafx-source-editor" -> "Source, code and query editor surfaces for KoraFX applications."
        "korafx-workspace" -> "Workspace layout and tabbed workbench surfaces for KoraFX applications."
        "korafx-test" -> "TestFX-backed JavaFX testing utilities for KoraFX modules and applications."
        "korafx-devtools" -> "Optional Chrome DevTools-inspired inspector for KoraFX applications."
        "korafx-macos" -> "Optional macOS native window chrome bridge for KoraFX applications."
        else -> "KoraFX module for Kotlin-friendly JavaFX development."
    }

fun Project.applyPublishingPropsFromDotenv() {
    val envExt = rootProject.extensions.findByName("env") ?: return
    val fetchOrNullMethod = envExt.javaClass.getMethod("fetchOrNull", String::class.java)

    publishingPropertyKeys.forEach { key ->
        val value = fetchOrNullMethod.invoke(envExt, key) as String?
        if (!value.isNullOrBlank()) {
            extensions.extraProperties.set(key, value)
        }
    }
}

fun Project.stringPropertyOrDefault(
    name: String,
    defaultValue: () -> String,
): Provider<String> = provider {
    (findProperty(name) as String?)?.takeIf(String::isNotBlank) ?: defaultValue()
}
