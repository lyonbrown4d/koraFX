package dev.korafx.framework

import dev.korafx.framework.theme.SceneThemeController
import dev.korafx.framework.theme.ThemeManager
import dev.korafx.navigation.Navigator
import dev.korafx.navigation.Route
import javafx.application.Application
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
import java.util.concurrent.atomic.AtomicReference
import java.util.prefs.Preferences

private val pendingApplication = AtomicReference<KoraApplicationSpec?>()

internal fun launchKoraApplication(
    args: Array<String>,
    spec: KoraApplicationSpec,
) {
    check(pendingApplication.compareAndSet(null, spec)) {
        "A KoraFX application is already pending launch."
    }

    try {
        Application.launch(KoraFxApplication::class.java, *args)
    } finally {
        pendingApplication.set(null)
    }
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
