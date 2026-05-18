package dev.korafx.framework

import dev.korafx.framework.navigation.Navigator
import dev.korafx.framework.navigation.Route
import dev.korafx.framework.theme.SceneThemeController
import dev.korafx.framework.theme.ThemeManager
import org.koin.core.module.Module
import org.koin.dsl.module

data class KoraWindowConfig(
    val title: String = "KoraFX",
    val width: Double = 1120.0,
    val height: Double = 720.0,
)

class KoraAppServices<R : Route>(
    val themeManager: ThemeManager,
    val navigator: Navigator<R>,
    val sceneThemeController: SceneThemeController,
)

fun <R : Route> koraFrameworkModule(
    initialRoute: R,
    routes: List<R>,
    themeManager: ThemeManager = ThemeManager(),
): Module =
    module {
        single { themeManager }
        single {
            Navigator(
                initialRoute = initialRoute,
                routes = routes,
            )
        }
        single { SceneThemeController(get()) }
        single { KoraAppServices<R>(get(), get(), get()) }
    }
