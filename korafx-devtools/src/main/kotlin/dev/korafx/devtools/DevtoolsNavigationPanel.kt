package dev.korafx.devtools

import dev.korafx.components.setKoraIcon
import dev.korafx.dsl.borderPane
import dev.korafx.dsl.listView
import dev.korafx.dsl.onAction
import dev.korafx.dsl.textArea
import dev.korafx.dsl.textField
import dev.korafx.framework.KoraApplication
import dev.korafx.navigation.PathRoute
import dev.korafx.navigation.Route
import javafx.collections.FXCollections
import javafx.scene.Node
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons

internal fun createDevtoolsNavigationPanel(
    app: KoraApplication,
    messages: DevtoolsMessages,
    jobSink: (Job) -> Unit,
): Node {
    val routeList = listView<RouteRow> {
        render { item ->
            if (item.active) {
                "-> ${item.route.title} (${item.route.id}) ${item.path}"
            } else {
                "${item.route.title} (${item.route.id}) ${item.path}"
            }
        }
    }
    val pathField = textField {
        promptText = "/path?query=value#hash"
        prefColumnCount = 24
    }
    val stateArea = textArea {
        isEditable = false
        prefRowCount = 10
    }

    fun refresh() {
        val state = app.navigator.state.value
        routeList.items = FXCollections.observableArrayList(
            state.routes.map { route ->
                RouteRow(
                    route = route,
                    path = route.pathLabel(),
                    active = route.id == state.currentRoute.id,
                )
            },
        )
        if (!pathField.isFocused) {
            pathField.text = state.currentLocation.fullPath
        }
        stateArea.text = buildString {
            appendLine("${messages.currentRoute} = ${state.currentRoute.title} (${state.currentRoute.id})")
            appendLine("${messages.currentLocation} = ${state.currentLocation.fullPath}")
            appendLine("${messages.routePath} = ${state.currentLocation.path}")
            if (state.currentLocation.params.isNotEmpty()) {
                appendLine("params = ${state.currentLocation.params}")
            }
            if (state.currentLocation.query.values.isNotEmpty()) {
                appendLine("query = ${state.currentLocation.query.values}")
            }
            state.currentLocation.hash?.let { hash ->
                appendLine("hash = $hash")
            }
            appendLine("${messages.pageInstancePolicy} = ${state.pageInstancePolicy}")
            appendLine("${messages.routes} = ${state.routes.size}")
            appendLine("${messages.backStack} = ${state.backStack.map { it.fullPath }}")
            appendLine("${messages.forwardStack} = ${state.forwardStack.map { it.fullPath }}")
        }
    }

    jobSink(
        app.uiScope.launch {
            app.navigator.state.collectLatest {
                refresh()
            }
        },
    )
    refresh()

    return borderPane {
        top {
            devtoolsToolbar(messages.registeredRoutes) {
                add(pathField)
                button(messages.navigatePath) {
                    setKoraIcon(BootstrapIcons.SIGNPOST_SPLIT)
                    onAction {
                        app.uiScope.launch {
                            app.navigator.navigatePathAsync(pathField.text)
                        }
                    }
                }
                button(messages.navigate) {
                    setKoraIcon(BootstrapIcons.ARROW_RIGHT_CIRCLE)
                    onAction {
                        routeList.selectionModel.selectedItem?.route?.let { route ->
                            app.uiScope.launch {
                                app.navigator.navigateAsync(route.id)
                            }
                        }
                    }
                }
                button(messages.back) {
                    setKoraIcon(BootstrapIcons.ARROW_LEFT_CIRCLE)
                    onAction {
                        app.uiScope.launch {
                            app.navigator.backAsync()
                        }
                    }
                }
                button(messages.forward) {
                    setKoraIcon(BootstrapIcons.ARROW_RIGHT_CIRCLE)
                    onAction {
                        app.uiScope.launch {
                            app.navigator.forwardAsync()
                        }
                    }
                }
                button(messages.refresh) {
                    setKoraIcon(BootstrapIcons.ARROW_CLOCKWISE)
                    onAction {
                        refresh()
                    }
                }
            }
        }
        center(routeList)
        bottom(stateArea)
    }
}

private fun Route.pathLabel(): String =
    (this as? PathRoute)?.path ?: id
