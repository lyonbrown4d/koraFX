package dev.korafx.framework

import dev.korafx.framework.theme.KoraTheme
import dev.korafx.framework.theme.ThemeManager
import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.Route
import javafx.scene.Node
import javafx.scene.Parent
import java.util.prefs.Preferences

data class KoraWindowSpec(
    val title: String = "KoraFX",
    val width: Double = 1120.0,
    val height: Double = 720.0,
    val minWidth: Double = 360.0,
    val minHeight: Double = 240.0,
    val resizable: Boolean = true,
    val titleBar: KoraWindowTitleBarSpec = KoraWindowTitleBarSpec(),
)

data class KoraWindowTitleBarSpec(
    val enabled: Boolean = false,
    val title: String? = null,
    val subtitle: String? = null,
    val height: Double = 42.0,
    val showMinimize: Boolean = true,
    val showMaximize: Boolean = true,
    val showClose: Boolean = true,
    val showTitle: Boolean = true,
    val chromeMode: KoraWindowChromeMode = KoraWindowChromeMode.AUTO,
    val controlSide: KoraWindowControlSide = KoraWindowControlSide.AUTO,
    val dragToMove: Boolean = true,
    val doubleClickMaximize: Boolean = true,
    val resizeEdges: Boolean = true,
    val resizeBorderWidth: Double = 6.0,
    val cornerRadius: Double = 0.0,
    val transparentBackground: Boolean = false,
    val dragOpacity: Double = 1.0,
    val nativeOptions: Map<String, Any> = emptyMap(),
    val contentFactory: (KoraApplication.() -> Node)? = null,
) {
    inline fun <reified T : Any> nativeOption(key: String): T? =
        nativeOptions[key] as? T
}

enum class KoraWindowChromeMode {
    AUTO,
    CUSTOM,
    NATIVE_OVERLAY,
    NATIVE,
}

enum class KoraWindowControlSide {
    AUTO,
    LEFT,
    RIGHT,
}

internal data class KoraApplicationSpec(
    val window: KoraWindowSpec,
    val modules: List<org.koin.core.module.Module>,
    val theme: KoraThemeSpec,
    val navigation: KoraNavigationSpec,
    val contentFactory: (KoraApplication.() -> Parent)?,
    val plugins: List<KoraApplicationPlugin>,
    val stopHandlers: List<KoraApplication.() -> Unit>,
)

internal data class KoraThemeSpec(
    val presets: List<KoraTheme>,
    val defaultTheme: KoraTheme,
    val persistSelection: Boolean,
    val preferencesNode: String,
    val preferencesKey: String,
) {
    fun createManager(): ThemeManager {
        val selectedTheme =
            if (persistSelection) {
                val savedThemeId = Preferences.userRoot().node(preferencesNode).get(preferencesKey, null)
                presets.firstOrNull { it.id == savedThemeId }
            } else {
                null
            }

        return ThemeManager(
            initialTheme = selectedTheme ?: defaultTheme,
            availableThemes = presets,
        )
    }
}

internal data class KoraNavigationSpec(
    val initialRoute: Route,
    val initialPath: String?,
    val routes: List<Route>,
    val pageInstancePolicy: PageInstancePolicy,
    val persistLocation: Boolean,
    val preferencesNode: String,
    val preferencesKey: String,
) {
    fun resolveInitialPath(): String? =
        if (persistLocation) {
            Preferences.userRoot().node(preferencesNode).get(preferencesKey, initialPath)
        } else {
            initialPath
        }
}

internal data object KoraRootRoute : Route {
    override val id: String = "root"
    override val title: String = "Root"
}
