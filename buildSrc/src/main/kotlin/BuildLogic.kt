import org.gradle.api.Project
import org.gradle.api.provider.Provider

private val publishableModules =
    setOf(
        "korafx-bom",
        "korafx-framework",
        "korafx-navigation",
        "korafx-dsl",
        "korafx-components",
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
        "korafx-navigation" -> "korafx-navigation"
        "korafx-dsl" -> "korafx-dsl"
        "korafx-components" -> "korafx-components"
        "korafx-devtools" -> "korafx-devtools"
        "korafx-macos" -> "korafx-macos"
        else -> name
    }

fun Project.publishedDescription(): String =
    when (name) {
        "korafx-bom" -> "Bill of materials for aligning KoraFX module versions."
        "korafx-navigation" -> "Navigation primitives for KoraFX route-based JavaFX applications."
        "korafx-dsl" -> "Kotlin-first JavaFX DSL and Flow state binding primitives."
        "korafx-framework" -> "Kotlin-first JavaFX application framework with Koin, MVVM, navigation and theme services."
        "korafx-components" -> "Reusable JavaFX workbench components for KoraFX applications."
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
