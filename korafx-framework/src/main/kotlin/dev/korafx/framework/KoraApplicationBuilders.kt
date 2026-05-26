package dev.korafx.framework

import dev.korafx.framework.theme.BuiltInThemes
import dev.korafx.framework.theme.KoraTheme
import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.Route
import javafx.scene.Node
import javafx.scene.Parent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import org.koin.core.module.Module

class KoraApplicationBuilder {
    private val windowBuilder = KoraWindowBuilder()
    private val koinBuilder = KoraKoinBuilder()
    private val themeBuilder = KoraThemeBuilder()
    private val navigationBuilder = KoraNavigationBuilder()
    private val lifecycleBuilder = KoraLifecycleBuilder()
    private val plugins = mutableListOf<KoraApplicationPlugin>()
    private var contentFactory: (KoraApplication.() -> Parent)? = null

    fun window(configure: KoraWindowBuilder.() -> Unit) {
        windowBuilder.configure()
    }

    fun installKoin(configure: KoraKoinBuilder.() -> Unit) {
        koinBuilder.configure()
    }

    fun theme(configure: KoraThemeBuilder.() -> Unit) {
        themeBuilder.configure()
    }

    fun navigation(configure: KoraNavigationBuilder.() -> Unit) {
        navigationBuilder.configure()
    }

    fun content(factory: KoraApplication.() -> Parent) {
        contentFactory = factory
    }

    fun lifecycle(configure: KoraLifecycleBuilder.() -> Unit) {
        lifecycleBuilder.configure()
    }

    fun install(plugin: KoraApplicationPlugin) {
        plugins += plugin
    }

    internal fun build(): KoraApplicationSpec =
        KoraApplicationSpec(
            window = windowBuilder.build(),
            modules = koinBuilder.modules,
            theme = themeBuilder.build(),
            navigation = navigationBuilder.build(),
            contentFactory = contentFactory,
            plugins = plugins.toList(),
            stopHandlers = lifecycleBuilder.build(),
        )
}

interface KoraApplicationPlugin {
    fun modules(app: KoraApplication): List<Module> = emptyList()

    fun onStart(app: KoraApplication) = Unit

    fun onStop(app: KoraApplication) = Unit
}

class KoraWindowBuilder {
    private val titleBarBuilder = KoraWindowTitleBarBuilder()

    var title: String = "KoraFX"
    var width: Double = 1120.0
    var height: Double = 720.0
    var minWidth: Double = 360.0
    var minHeight: Double = 240.0
    var resizable: Boolean = true

    fun size(
        width: Double,
        height: Double,
    ) {
        this.width = width
        this.height = height
    }

    fun minSize(
        width: Double,
        height: Double,
    ) {
        minWidth = width
        minHeight = height
    }

    fun titleBar(configure: KoraWindowTitleBarBuilder.() -> Unit = {}) {
        titleBarBuilder.enabled = true
        titleBarBuilder.configure()
    }

    internal fun build(): KoraWindowSpec {
        require(width > 0.0 && height > 0.0) {
            "KoraFX window size must be positive."
        }
        require(minWidth > 0.0 && minHeight > 0.0) {
            "KoraFX minimum window size must be positive."
        }

        return KoraWindowSpec(
            title = title,
            width = width,
            height = height,
            minWidth = minWidth,
            minHeight = minHeight,
            resizable = resizable,
            titleBar = titleBarBuilder.build(),
        )
    }
}

class KoraWindowTitleBarBuilder {
    var enabled: Boolean = false
    var title: String? = null
    var subtitle: String? = null
    var height: Double = 42.0
    var showMinimize: Boolean = true
    var showMaximize: Boolean = true
    var showClose: Boolean = true
    var showTitle: Boolean = true
    var chromeMode: KoraWindowChromeMode = KoraWindowChromeMode.AUTO
    var controlSide: KoraWindowControlSide = KoraWindowControlSide.AUTO
    var dragToMove: Boolean = true
    var doubleClickMaximize: Boolean = true
    var resizeEdges: Boolean = true
    var resizeBorderWidth: Double = 6.0
    var cornerRadius: Double = 0.0
    var transparentBackground: Boolean = false
    var dragOpacity: Double = 1.0
    private val nativeOptions = linkedMapOf<String, Any>()
    private var contentFactory: (KoraApplication.() -> Node)? = null

    fun content(factory: KoraApplication.() -> Node) {
        contentFactory = factory
    }

    fun nativeOption(
        key: String,
        value: Any,
    ) {
        require(key.isNotBlank()) {
            "KoraFX native title bar option key cannot be blank."
        }

        nativeOptions[key] = value
    }

    internal fun build(): KoraWindowTitleBarSpec {
        require(height > 0.0) {
            "KoraFX title bar height must be positive."
        }
        require(resizeBorderWidth >= 0.0) {
            "KoraFX resize border width must be non-negative."
        }
        require(cornerRadius >= 0.0) {
            "KoraFX title bar corner radius must be non-negative."
        }
        require(dragOpacity in 0.0..1.0) {
            "KoraFX drag opacity must be between 0.0 and 1.0."
        }

        return KoraWindowTitleBarSpec(
            enabled = enabled,
            title = title,
            subtitle = subtitle,
            height = height,
            showMinimize = showMinimize,
            showMaximize = showMaximize,
            showClose = showClose,
            showTitle = showTitle,
            chromeMode = chromeMode,
            controlSide = controlSide,
            dragToMove = dragToMove,
            doubleClickMaximize = doubleClickMaximize,
            resizeEdges = resizeEdges,
            resizeBorderWidth = resizeBorderWidth,
            cornerRadius = cornerRadius,
            transparentBackground = transparentBackground,
            dragOpacity = dragOpacity,
            nativeOptions = nativeOptions.toMap(),
            contentFactory = contentFactory,
        )
    }
}

class KoraKoinBuilder {
    internal val modules = mutableListOf<Module>()

    fun module(module: Module) {
        modules += module
    }

    fun modules(vararg modules: Module) {
        this.modules += modules
    }

    fun modules(modules: Iterable<Module>) {
        this.modules += modules
    }
}

class KoraThemeBuilder {
    private var presets: List<KoraTheme> = BuiltInThemes.all
    private var defaultTheme: KoraTheme = BuiltInThemes.MaterialLight
    var persistSelection: Boolean = false
    var preferencesNode: String = "dev.korafx"
    var preferencesKey: String = "theme"

    fun presets(themes: Iterable<KoraTheme>) {
        presets = themes.toList()
        require(presets.isNotEmpty()) {
            "KoraFX theme presets cannot be empty."
        }
    }

    fun default(theme: KoraTheme) {
        defaultTheme = theme
    }

    internal fun build(): KoraThemeSpec {
        require(presets.any { it.id == defaultTheme.id }) {
            "The default theme must be part of the available theme presets."
        }

        return KoraThemeSpec(
            presets = presets,
            defaultTheme = defaultTheme,
            persistSelection = persistSelection,
            preferencesNode = preferencesNode,
            preferencesKey = preferencesKey,
        )
    }
}
