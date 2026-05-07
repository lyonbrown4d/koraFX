import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.plugins.JavaPlatformPlugin

plugins {
    alias(libs.plugins.dotenv) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.openjfx) apply false
}

apply(plugin = libs.plugins.dotenv.get().pluginId)

group = "io.github.daiyuang"
version = providers.gradleProperty("releaseVersion").orElse("0.1.0-SNAPSHOT").get()

subprojects {
    group = rootProject.group
    version = rootProject.version

    if (isPublishableLeafModule()) {
        apply<com.vanniktech.maven.publish.MavenPublishPlugin>()
        if (isBomModule()) {
            apply<JavaPlatformPlugin>()
        }
        applyPublishingPropsFromDotenv()

        extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
            coordinates(rootProject.group.toString(), publishedArtifactId(), project.version.toString())
            publishToMavenCentral()
            signAllPublications()
            pom {
                name.set(stringPropertyOrDefault("POM_NAME") { publishedArtifactId() })
                description.set(
                    stringPropertyOrDefault("POM_DESCRIPTION") {
                        publishedDescription()
                    }
                )
                inceptionYear.set(stringPropertyOrDefault("POM_INCEPTION_YEAR") { "2026" })
                url.set(stringPropertyOrDefault("POM_URL") { "https://github.com/DaiYuANg/koraFX" })

                licenses {
                    license {
                        name.set(
                            stringPropertyOrDefault("POM_LICENSE_NAME") {
                                "The Apache License, Version 2.0"
                            }
                        )
                        url.set(
                            stringPropertyOrDefault("POM_LICENSE_URL") {
                                "https://www.apache.org/licenses/LICENSE-2.0.txt"
                            }
                        )
                        distribution.set(stringPropertyOrDefault("POM_LICENSE_DIST") { "repo" })
                    }
                }
                developers {
                    developer {
                        id.set(stringPropertyOrDefault("POM_DEVELOPER_ID") { "DaiYuANg" })
                        name.set(stringPropertyOrDefault("POM_DEVELOPER_NAME") { "DaiYuANg" })
                        url.set(stringPropertyOrDefault("POM_DEVELOPER_URL") { "https://github.com/DaiYuANg" })
                    }
                }
                scm {
                    val defaultScmUrl = "https://github.com/DaiYuANg/koraFX"
                    val projectScmUrl = stringPropertyOrDefault("POM_SCM_URL") {
                        defaultScmUrl
                    }
                    url.set(projectScmUrl)
                    connection.set(
                        stringPropertyOrDefault("POM_SCM_CONNECTION") {
                            "scm:git:$defaultScmUrl.git"
                        }
                    )
                    developerConnection.set(
                        stringPropertyOrDefault("POM_SCM_DEV_CONNECTION") {
                            "scm:git:ssh://git@github.com/DaiYuANg/koraFX.git"
                        }
                    )
                }
            }
        }

    }
}
