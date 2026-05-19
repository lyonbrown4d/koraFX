package dev.korafx.framework

import dev.korafx.framework.navigation.Navigator
import dev.korafx.framework.navigation.PageInstancePolicy
import dev.korafx.framework.navigation.Route
import dev.korafx.framework.theme.BuiltInThemes
import dev.korafx.framework.theme.KoraTheme
import dev.korafx.framework.theme.SceneThemeController
import dev.korafx.framework.theme.ThemeManager
import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
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

internal data class KoraWindowSpec(
    val title: String = "KoraFX",
    val width: Double = 1120.0,
    val height: Double = 720.0,
)

internal fun <R : Route> koraFrameworkModule(
    initialRoute: R,
    routes: List<R>,
    pageInstancePolicy: PageInstancePolicy = PageInstancePolicy.RECREATE,
    themeManager: ThemeManager = ThemeManager(),
): Module =
    module {
        single { themeManager }
        single {
            Navigator(
                initialRoute = initialRoute,
                routes = routes,
                pageInstancePolicy = pageInstancePolicy,
            )
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

    internal fun build(): KoraApplicationSpec =
        KoraApplicationSpec(
            window = windowBuilder.build(),
            modules = koinBuilder.modules,
            theme = themeBuilder.build(),
            navigation = navigationBuilder.build(),
            contentFactory = contentFactory,
            stopHandlers = lifecycleBuilder.build(),
        )
}

class KoraWindowBuilder {
    var title: String = "KoraFX"
    var width: Double = 1120.0
    var height: Double = 720.0

    fun size(
        width: Double,
        height: Double,
    ) {
        this.width = width
        this.height = height
    }

    internal fun build(): KoraWindowSpec =
        KoraWindowSpec(
            title = title,
            width = width,
            height = height,
        )
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
    private var defaultTheme: KoraTheme = BuiltInThemes.Light
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
    var pageInstancePolicy: PageInstancePolicy = PageInstancePolicy.RECREATE
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
            routes = routeList,
            pageInstancePolicy = pageInstancePolicy,
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
    private var sceneValue: Scene? = null

    val uiScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.JavaFx)

    val koin: Koin
        get() = koinApplication.koin

    val scene: Scene
        get() = sceneValue ?: error("Scene is not attached yet.")

    inline fun <reified T : Any> get(): T = koin.get()

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
    }

    override fun close() {
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
    val routes: List<Route>,
    val pageInstancePolicy: PageInstancePolicy,
)

private data object KoraRootRoute : Route {
    override val id: String = "root"
    override val title: String = "Root"
}

private val pendingApplication = AtomicReference<KoraApplicationSpec?>()

class KoraFxApplication : Application() {
    private var app: KoraApplication? = null

    override fun start(stage: Stage) {
        val spec = pendingApplication.get() ?: error("No KoraFX application configuration found.")
        val themeManager = spec.theme.createManager()
        val frameworkModule = koraFrameworkModule(
            initialRoute = spec.navigation.initialRoute,
            routes = spec.navigation.routes,
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

        val root = spec.contentFactory?.invoke(koraApp) ?: Pane()
        val scene = Scene(root, spec.window.width, spec.window.height)
        koraApp.attach(scene)
        koraApp.themeController.bind(scene)
        koraApp.start()

        stage.title = spec.window.title
        stage.scene = scene
        stage.show()
    }

    override fun stop() {
        app?.close()
        app = null
    }
}
