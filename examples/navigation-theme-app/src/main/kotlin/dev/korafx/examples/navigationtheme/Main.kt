package dev.korafx.examples.navigationtheme

import dev.korafx.components.actionBar
import dev.korafx.components.appShell
import dev.korafx.components.emptyState
import dev.korafx.components.modalHost
import dev.korafx.components.ModalAction
import dev.korafx.components.ModalActionRole
import dev.korafx.components.ModalHost
import dev.korafx.components.navigationRail
import dev.korafx.components.routeHost
import dev.korafx.components.section
import dev.korafx.components.toastHost
import dev.korafx.components.ToastHost
import dev.korafx.components.ToastTone
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.hbox
import dev.korafx.dsl.menuButton
import dev.korafx.dsl.onAction
import dev.korafx.dsl.paddingAll
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.toolbar
import dev.korafx.dsl.vbox
import dev.korafx.framework.navigation.Navigator
import dev.korafx.framework.navigation.PageInstancePolicy
import dev.korafx.framework.navigation.Route
import dev.korafx.framework.theme.BuiltInThemes
import dev.korafx.framework.theme.KoraTheme
import dev.korafx.framework.theme.SceneThemeController
import dev.korafx.framework.theme.ThemeManager
import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
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
    private val notifications = ToastHost()
    private val modals = ModalHost()
    private val navigator = Navigator(
        initialRoute = DemoRoute.Dashboard,
        routes = DemoRoute.all,
        pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
    )

    override fun start(stage: Stage) {
        val root = appShell {
            topBar {
                toolbar {
                    label("KoraFX Navigation + Theme") {
                        styleClasses("headline")
                    }
                    spacer()
                    ghostButton("Next Theme") {
                        onAction {
                            nextTheme()
                        }
                    }
                    menuButton(
                        text = "Themes",
                        content = {
                            BuiltInThemes.all.forEach { theme ->
                                actionItem(theme.displayName) {
                                    setTheme(theme)
                                }
                            }
                        },
                    )
                }
            }
            navigation {
                navigationRail(uiScope, navigator)
            }
            content {
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
            overlay {
                toastHost(uiScope, notifications)
            }
            overlay(alignment = Pos.CENTER, margin = Insets.EMPTY) {
                modalHost(uiScope, modals)
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
                    notifications.show(
                        message = "Opened Tasks route.",
                        tone = ToastTone.INFO,
                    )
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
                    notifications.show(
                        message = "Opened Settings route.",
                        tone = ToastTone.INFO,
                    )
                }
            }
        }
    }

    private fun dev.korafx.dsl.VBoxBuilder.settingsContent() {
        emptyState(
            title = "Settings",
            message = "Open an in-scene modal rendered by modalHost.",
            actionText = "Open Settings",
            onAction = {
                openSettingsModal()
            },
        )
    }

    private fun openSettingsModal() {
        modals.show(
            title = "Workspace settings",
            message = "This modal is rendered inside appShell.overlay without taking over application lifecycle.",
            actions = listOf(
                ModalAction("Cancel"),
                ModalAction(
                    text = "Apply",
                    role = ModalActionRole.PRIMARY,
                    onAction = {
                        notifications.show(
                            message = "Settings saved.",
                            tone = ToastTone.SUCCESS,
                        )
                    },
                ),
            ),
        ) {
            label("Current theme")
            label(themeManager.currentTheme().displayName) {
                styleClasses("muted")
            }
            label("Available built-in themes")
            BuiltInThemes.all.forEach { theme ->
                label("- ${theme.displayName}") {
                    styleClasses("muted")
                }
            }
        }
    }

    private fun nextTheme() {
        themeManager.nextTheme()
        notifyThemeChanged()
    }

    private fun setTheme(theme: KoraTheme) {
        themeManager.setTheme(theme)
        notifyThemeChanged()
    }

    private fun notifyThemeChanged() {
        notifications.show(
            message = "Theme switched to ${themeManager.currentTheme().displayName}.",
            tone = ToastTone.SUCCESS,
        )
    }

    override fun stop() {
        uiScope.cancel()
        themeController.dispose()
    }
}
