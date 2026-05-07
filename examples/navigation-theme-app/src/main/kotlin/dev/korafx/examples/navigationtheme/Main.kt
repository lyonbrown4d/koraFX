package dev.korafx.examples.navigationtheme

import dev.korafx.components.actionBar
import dev.korafx.components.emptyState
import dev.korafx.components.navigationRail
import dev.korafx.components.routeHost
import dev.korafx.components.section
import dev.korafx.dsl.borderPane
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.hbox
import dev.korafx.dsl.onAction
import dev.korafx.dsl.paddingAll
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.toolbar
import dev.korafx.dsl.vbox
import dev.korafx.navigation.Navigator
import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.Route
import dev.korafx.theme.SceneThemeController
import dev.korafx.theme.ThemeManager
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Scene
import javafx.stage.Stage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

fun main(args: Array<String>) {
    Application.launch(NavigationThemeApp::class.java, *args)
}

private enum class DemoRoute(
    override val id: String,
    override val title: String,
) : Route {
    Dashboard("dashboard", "Dashboard"),
    Tasks("tasks", "Tasks"),
    Settings("settings", "Settings");

    companion object {
        val all: List<DemoRoute>
            get() = entries.toList()
    }
}

class NavigationThemeApp : Application() {
    private val uiScope = MainScope()
    private val themeManager = ThemeManager()
    private val themeController = SceneThemeController(themeManager)
    private val navigator = Navigator(
        initialRoute = DemoRoute.Dashboard,
        routes = DemoRoute.all,
        pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
    )

    override fun start(stage: Stage) {
        val root = borderPane {
            top {
                toolbar {
                    label("KoraFX Navigation + Theme") {
                        styleClasses("headline")
                    }
                    spacer()
                    ghostButton("Toggle Theme") {
                        onAction {
                            themeManager.toggle()
                        }
                    }
                }
            }
            left {
                navigationRail(uiScope, navigator)
            }
            center {
                routeHost(
                    scope = uiScope,
                    navigator = navigator,
                    init = {
                        paddingAll(24.0)
                    },
                ) { route ->
                    routeContent(route)
                }
            }
        }

        val scene = Scene(root, 920.0, 560.0)
        themeController.bind(scene)

        stage.title = "KoraFX Navigation Theme"
        stage.scene = scene
        stage.show()
    }

    private fun routeContent(route: DemoRoute): Node =
        vbox(
            spacing = 18.0,
            init = {
                padding = Insets(0.0)
            },
        ) {
            section(
                title = route.title,
                description = "This page is rendered by routeHost and selected by navigationRail.",
            ) {
                when (route) {
                    DemoRoute.Dashboard -> dashboardContent()
                    DemoRoute.Tasks -> tasksContent()
                    DemoRoute.Settings -> settingsContent()
                }
            }
        }

    private fun dev.korafx.dsl.VBoxBuilder.dashboardContent() {
        label("Dashboard content stays as plain JavaFX nodes.")
        hbox(spacing = 12.0) {
            label("Active route")
            label(DemoRoute.Dashboard.id) {
                styleClasses("muted")
            }
        }
        actionBar(alignEnd = false) {
            button("Open Tasks") {
                onAction {
                    navigator.navigate(DemoRoute.Tasks)
                }
            }
        }
    }

    private fun dev.korafx.dsl.VBoxBuilder.tasksContent() {
        label("Task list")
        vbox(spacing = 8.0) {
            listOf("Wire navigation", "Bind scene theme", "Keep route pages alive").forEach { task ->
                label("- $task")
            }
        }
        actionBar(alignEnd = false) {
            button("Open Settings") {
                onAction {
                    navigator.navigate(DemoRoute.Settings)
                }
            }
        }
    }

    private fun dev.korafx.dsl.VBoxBuilder.settingsContent() {
        emptyState(
            title = "Settings",
            message = "Use the toolbar action to toggle the active KoraFX theme.",
            actionText = "Toggle Theme",
            onAction = {
                themeManager.toggle()
            },
        )
    }

    override fun stop() {
        uiScope.cancel()
        themeController.dispose()
    }
}
