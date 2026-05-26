package dev.korafx.framework

import dev.korafx.framework.theme.SceneThemeController
import dev.korafx.framework.theme.ThemeManager
import dev.korafx.navigation.Navigator
import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.Route
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun <R : Route> koraFrameworkModule(
    initialRoute: R,
    routes: List<R>,
    initialPath: String? = null,
    pageInstancePolicy: PageInstancePolicy = PageInstancePolicy.RECREATE,
    themeManager: ThemeManager = ThemeManager(),
): Module =
    module {
        single { themeManager }
        single {
            if (initialPath.isNullOrBlank()) {
                Navigator(
                    initialRoute = initialRoute,
                    routes = routes,
                    pageInstancePolicy = pageInstancePolicy,
                )
            } else {
                Navigator.fromPath(
                    initialPath = initialPath,
                    routes = routes,
                    fallbackRoute = initialRoute,
                    pageInstancePolicy = pageInstancePolicy,
                )
            }
        }
        single { SceneThemeController(get()) }
    }

fun koraApplication(configure: KoraApplicationBuilder.() -> Unit) {
    koraApplication(emptyArray(), configure)
}

fun koraApplication(
    args: Array<String>,
    configure: KoraApplicationBuilder.() -> Unit,
) {
    val spec = KoraApplicationBuilder().apply(configure).build()
    launchKoraApplication(args, spec)
}
