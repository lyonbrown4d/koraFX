package dev.korafx.devtools

import dev.korafx.components.ComponentTone
import dev.korafx.components.badge
import dev.korafx.components.setKoraIcon
import dev.korafx.components.workspaceLayout
import dev.korafx.dsl.hbox
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.padding
import dev.korafx.dsl.styleClass
import dev.korafx.framework.KoraApplication
import dev.korafx.navigation.Navigator
import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.navigationRail
import dev.korafx.navigation.routeHost
import dev.korafx.framework.theme.ThemeStyleClass
import javafx.scene.Node
import javafx.scene.Parent
import kotlinx.coroutines.Job
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons

internal class DevtoolsShell(
    private val app: KoraApplication,
    private val spec: KoraDevtoolsSpec,
    private val messages: DevtoolsMessages,
    private val selection: DevtoolsSelectionModel,
    private val actions: DevtoolsActions,
    private val jobSink: (Job) -> Unit,
    private val inspectedRoot: () -> Parent,
) {
    fun build(): Parent {
        val routes = spec.panels.map { panel -> panel.toRoute(messages) }
        val navigator = Navigator(
            initialRoute = routes.first(),
            routes = routes,
            pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
        )
        val panels = DevtoolsPanels(
            app = app,
            inspectedRoot = inspectedRoot,
            messages = messages,
            selection = selection,
            actions = actions,
            jobSink = jobSink,
        )

        return workspaceLayout(
            init = {
                styleClass += "korafx-devtools"
                overlayLayer.isMouseTransparent = true
            },
        ) {
            topBar(createHeader())
            navigation(
                navigationRail(
                    scope = app.uiScope,
                    navigator = navigator,
                    width = 230.0,
                    icon = DevtoolsRoute::icon,
                ) {
                    styleClass += "korafx-devtools-nav"
                },
            )
            content(
                routeHost(
                    scope = app.uiScope,
                    navigator = navigator,
                ) { route ->
                    panels.render(route.panel)
                },
            )
            status(createStatus())
        }
    }

    private fun createHeader(): Node =
        hbox(
            spacing = 10.0,
            init = {
                padding(12.0, 14.0)
                styleClass("korafx-devtools-header")
            },
        ) {
            label(messages.title) {
                styleClass(ThemeStyleClass.Headline)
                setKoraIcon(BootstrapIcons.TOOLS, size = 20)
            }
            badge(messages.subappBadge, ComponentTone.INFO, icon = BootstrapIcons.WINDOW_SIDEBAR)
            button(messages.dockLeft) {
                styleClass("ghost-button")
                onAction {
                    actions.setPlacement(KoraDevtoolsPlacement.LEFT)
                }
            }
            button(messages.dockBottom) {
                styleClass("ghost-button")
                onAction {
                    actions.setPlacement(KoraDevtoolsPlacement.BOTTOM)
                }
            }
            button(messages.dockRight) {
                styleClass("ghost-button")
                onAction {
                    actions.setPlacement(KoraDevtoolsPlacement.RIGHT)
                }
            }
            button(messages.openWindow) {
                styleClass("ghost-button")
                onAction {
                    actions.setPlacement(KoraDevtoolsPlacement.WINDOW)
                }
            }
            spacer()
            label(messages.shortcutHelp) {
                styleClass(ThemeStyleClass.Muted)
            }
        }

    private fun createStatus(): Node =
        hbox(
            spacing = 8.0,
            init = {
                padding(8.0, 14.0)
                styleClass("korafx-devtools-status")
            },
        ) {
            label(messages.statusText) {
                styleClass(ThemeStyleClass.Muted)
            }
        }
}
