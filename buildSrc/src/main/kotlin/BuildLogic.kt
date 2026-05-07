import org.gradle.api.Project
import org.gradle.api.provider.Provider

private val publishableModules =
    setOf(
        "framework-dsl",
        "framework-state",
        "framework-mvvm",
        "framework-navigation",
        "framework-theme",
        "framework-components",
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

fun Project.publishedArtifactId(): String =
    when (name) {
        "framework-dsl" -> "korafx-dsl"
        "framework-state" -> "korafx-state"
        "framework-mvvm" -> "korafx-mvvm"
        "framework-navigation" -> "korafx-navigation"
        "framework-theme" -> "korafx-theme"
        "framework-components" -> "korafx-components"
        else -> name
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
