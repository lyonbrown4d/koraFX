package dev.korafx.framework

import dev.korafx.navigation.Navigator
import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.Route
import dev.korafx.framework.theme.BuiltInThemes
import dev.korafx.framework.theme.KoraTheme
import dev.korafx.framework.theme.SceneThemeController
import dev.korafx.framework.theme.ThemeManager
import javafx.application.Application
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicReference
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
    check(pendingApplication.compareAndSet(null, spec)) {
        "A KoraFX application is already pending launch."
    }

    try {
        Application.launch(KoraFxApplication::class.java, *args)
    } finally {
        pendingApplication.set(null)
    }
}

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

class KoraNavigationBuilder {
    var initialRoute: Route? = null
    var initialPath: String? = null
    var pageInstancePolicy: PageInstancePolicy = PageInstancePolicy.RECREATE
    var persistLocation: Boolean = false
    var preferencesNode: String = "dev.korafx"
    var preferencesKey: String = "navigation.location"
    private val routes = mutableListOf<Route>()

    fun routes(routes: Iterable<Route>) {
        this.routes += routes
    }

    fun routes(vararg routes: Route) {
        this.routes += routes
    }

    internal fun build(): KoraNavigationSpec {
        val initial = initialRoute ?: routes.firstOrNull() ?: KoraRootRoute
        val routeList = (listOf(initial) + routes)
            .distinctBy(Route::id)

        return KoraNavigationSpec(
            initialRoute = initial,
            initialPath = initialPath,
            routes = routeList,
            pageInstancePolicy = pageInstancePolicy,
            persistLocation = persistLocation,
            preferencesNode = preferencesNode,
            preferencesKey = preferencesKey,
        )
    }
}

class KoraLifecycleBuilder {
    private val stopHandlers = mutableListOf<KoraApplication.() -> Unit>()

    fun onStop(handler: KoraApplication.() -> Unit) {
        stopHandlers += handler
    }

    inline fun <reified T : AutoCloseable> close() {
        onStop {
            get<T>().close()
        }
    }

    inline fun <reified T : CoroutineScope> cancel() {
        onStop {
            get<T>().cancel()
        }
    }

    internal fun build(): List<KoraApplication.() -> Unit> = stopHandlers.toList()
}

class KoraApplication internal constructor(
    private val spec: KoraApplicationSpec,
    private val koinApplication: KoinApplication,
    val stage: Stage,
    val themeManager: ThemeManager,
    val navigator: Navigator<Route>,
    val themeController: SceneThemeController,
) : AutoCloseable {
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activePlugins = mutableListOf<KoraApplicationPlugin>()
    private val pluginModules = linkedMapOf<KoraApplicationPlugin, List<Module>>()
    private var sceneValue: Scene? = null

    val uiScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.JavaFx)

    val koin: Koin
        get() = koinApplication.koin

    val scene: Scene
        get() = sceneValue ?: error("Scene is not attached yet.")

    val window: KoraWindowSpec
        get() = spec.window

    inline fun <reified T : Any> get(): T = koin.get()

    fun loadModules(modules: Iterable<Module>) {
        koin.loadModules(modules.toList())
    }

    fun loadModules(vararg modules: Module) {
        loadModules(modules.asIterable())
    }

    fun unloadModules(modules: Iterable<Module>) {
        koin.unloadModules(modules.toList())
    }

    fun unloadModules(vararg modules: Module) {
        unloadModules(modules.asIterable())
    }

    internal fun attach(scene: Scene) {
        sceneValue = scene
    }

    internal fun start() {
        if (spec.theme.persistSelection) {
            val preferences = Preferences.userRoot().node(spec.theme.preferencesNode)
            persistenceScope.launch {
                themeManager.theme.collectLatest { theme ->
                    preferences.put(spec.theme.preferencesKey, theme.id)
                }
            }
        }
        if (spec.navigation.persistLocation) {
            val preferences = Preferences.userRoot().node(spec.navigation.preferencesNode)
            persistenceScope.launch {
                navigator.state.collectLatest { navigation ->
                    preferences.put(spec.navigation.preferencesKey, navigation.currentLocation.fullPath)
                }
            }
        }
        spec.plugins.forEach { plugin ->
            runCatching {
                val modules = plugin.modules(this)
                if (modules.isNotEmpty()) {
                    loadModules(modules)
                    pluginModules[plugin] = modules
                }
                plugin.onStart(this)
                activePlugins += plugin
            }.onFailure { error ->
                pluginModules.remove(plugin)?.let(::unloadModules)
                System.err.println("KoraFX plugin start failed: ${error.message}")
                error.printStackTrace()
            }
        }
    }

    override fun close() {
        activePlugins.asReversed().forEach { plugin ->
            runCatching {
                plugin.onStop(this)
            }.onFailure { error ->
                System.err.println("KoraFX plugin stop failed: ${error.message}")
                error.printStackTrace()
            }
        }
        activePlugins.asReversed().forEach { plugin ->
            pluginModules.remove(plugin)?.let { modules ->
                runCatching {
                    unloadModules(modules)
                }.onFailure { error ->
                    System.err.println("KoraFX plugin module unload failed: ${error.message}")
                    error.printStackTrace()
                }
            }
        }
        spec.stopHandlers.asReversed().forEach { handler ->
            runCatching {
                handler()
            }.onFailure { error ->
                System.err.println("KoraFX stop handler failed: ${error.message}")
                error.printStackTrace()
            }
        }
        themeController.dispose()
        persistenceScope.cancel()
        uiScope.cancel()
        koinApplication.close()
    }
}

internal data class KoraApplicationSpec(
    val window: KoraWindowSpec,
    val modules: List<Module>,
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

private data object KoraRootRoute : Route {
    override val id: String = "root"
    override val title: String = "Root"
}

private val pendingApplication = AtomicReference<KoraApplicationSpec?>()

class KoraFxApplication : Application() {
    private var app: KoraApplication? = null

    override fun start(stage: Stage) {
        val spec = pendingApplication.get() ?: error("No KoraFX application configuration found.")
        val customChrome = spec.window.usesCustomChrome()
        if (customChrome) {
            stage.initStyle(
                if (spec.window.titleBar.transparentBackground || spec.window.titleBar.cornerRadius > 0.0) {
                    StageStyle.TRANSPARENT
                } else {
                    StageStyle.UNDECORATED
                },
            )
        }
        val themeManager = spec.theme.createManager()
        val frameworkModule = koraFrameworkModule(
            initialRoute = spec.navigation.initialRoute,
            routes = spec.navigation.routes,
            initialPath = spec.navigation.resolveInitialPath(),
            pageInstancePolicy = spec.navigation.pageInstancePolicy,
            themeManager = themeManager,
        )
        val koinApplication = startKoin {
            modules(listOf(frameworkModule) + spec.modules)
        }
        val koin = koinApplication.koin
        val koraApp = KoraApplication(
            spec = spec,
            koinApplication = koinApplication,
            stage = stage,
            themeManager = koin.get(),
            navigator = koin.get(),
            themeController = koin.get(),
        )
        app = koraApp

        stage.title = spec.window.title
        stage.isResizable = spec.window.resizable
        stage.minWidth = spec.window.minWidth
        stage.minHeight = spec.window.minHeight

        val contentRoot = spec.contentFactory?.invoke(koraApp) ?: Pane()
        val root =
            if (customChrome) {
                KoraWindowChrome.wrap(
                    app = koraApp,
                    stage = stage,
                    content = contentRoot,
                    spec = spec.window,
                )
            } else {
                contentRoot
            }
        val scene = Scene(root, spec.window.width, spec.window.height)
        if (customChrome && (spec.window.titleBar.transparentBackground || spec.window.titleBar.cornerRadius > 0.0)) {
            scene.fill = Color.TRANSPARENT
        }
        koraApp.attach(scene)
        koraApp.themeController.bind(scene)
        koraApp.start()

        stage.scene = scene
        stage.show()
    }

    override fun stop() {
        app?.close()
        app = null
    }
}

internal fun KoraWindowSpec.usesCustomChrome(): Boolean =
    titleBar.enabled &&
        when (titleBar.chromeMode) {
            KoraWindowChromeMode.CUSTOM -> true
            KoraWindowChromeMode.NATIVE -> false
            KoraWindowChromeMode.NATIVE_OVERLAY -> !isMacOs()
            KoraWindowChromeMode.AUTO -> !isMacOs()
        }

private fun isMacOs(): Boolean =
    System.getProperty("os.name").contains("mac", ignoreCase = true)
