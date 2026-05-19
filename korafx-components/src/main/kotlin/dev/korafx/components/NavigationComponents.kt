package dev.korafx.components

import dev.korafx.dsl.FragmentBuilder
import dev.korafx.dsl.RenderState
import dev.korafx.dsl.fragment
import dev.korafx.dsl.navButton
import dev.korafx.dsl.onAction
import dev.korafx.dsl.sidebar
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.vbox
import dev.korafx.framework.navigation.Navigator
import dev.korafx.framework.navigation.PageInstancePolicy
import dev.korafx.framework.navigation.Route
import dev.korafx.dsl.state.collectLatestIn
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.kordamp.ikonli.Ikon

data class NavigationRailItem<R : Route>(
    val route: R,
    val active: Boolean,
)

fun <R : Route> navigationRail(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    width: Double = 220.0,
    spacing: Double = 10.0,
    icon: (R) -> Ikon? = { null },
    iconSize: Int = 16,
    init: VBox.() -> Unit = {},
    buttonInit: Button.(NavigationRailItem<R>) -> Unit = {},
): VBox =
    sidebar(
        width = width,
        spacing = spacing,
        init = init,
    ) {}.also { rail ->
        navigator.state.collectLatestIn(scope) { state ->
            runOnFxThread {
                rail.children.setAll(
                    state.routes.map { route ->
                        val item = NavigationRailItem(
                            route = route,
                            active = route.id == state.currentRoute.id,
                        )

                        navButton(route.title, active = item.active) {
                            maxWidth = Double.MAX_VALUE
                            icon(route)?.let { routeIcon ->
                                setKoraIcon(routeIcon, iconSize)
                            }
                            onAction {
                                navigator.navigate(route.id)
                            }
                            buttonInit(item)
                        }
                    },
                )
            }
        }
    }

fun <R : Route> routeHost(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    init: StackPane.() -> Unit = {},
    content: (route: R) -> Node,
): StackPane {
    val cache = linkedMapOf<String, Node>()
    val host = StackPane().apply(init)

    navigator.state.collectLatestIn(scope) { state ->
        runOnFxThread {
            val route = state.currentRoute
            val node =
                when (state.pageInstancePolicy) {
                    PageInstancePolicy.RECREATE -> content(route)
                    PageInstancePolicy.KEEP_ALIVE,
                    PageInstancePolicy.SINGLETON_IN_WINDOW,
                    -> cache.getOrPut(route.id) { content(route) }
                }

            host.children.setAll(node)
        }
    }

    return host
}

fun <R : Route, T> routeStateHost(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    spacing: Double = 16.0,
    init: VBox.() -> Unit = {},
    stateFor: (route: R) -> Flow<RenderState<T>>,
    loading: FragmentBuilder.(route: R) -> Unit = { route ->
        add(loadingState("Loading ${route.title}..."))
    },
    empty: FragmentBuilder.(route: R) -> Unit = { route ->
        add(
            emptyState(
                title = route.title,
                message = "No content is available for this route.",
            ),
        )
    },
    failed: FragmentBuilder.(route: R, failure: RenderState.Failed) -> Unit = { route, failure ->
        add(
            errorState(
                title = "${route.title} failed to load",
                message = failure.message,
            ),
        )
    },
    content: FragmentBuilder.(route: R, value: T) -> Unit,
): VBox =
    vbox(
        spacing = spacing,
        init = {
            styleClass("route-state-host")
            init()
        },
    ) {}.also { host ->
        navigator.state.collectLatestIn(scope) { navigation ->
            val route = navigation.currentRoute
            stateFor(route).collectLatest { state ->
                runOnFxThread {
                    host.children.setAll(
                        fragment {
                            when (state) {
                                RenderState.Loading -> loading(route)
                                RenderState.Empty -> empty(route)
                                is RenderState.Failed -> failed(route, state)
                                is RenderState.Content -> content(route, state.value)
                            }
                        },
                    )
                }
            }
        }
    }

private fun runOnFxThread(block: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        block()
    } else {
        Platform.runLater {
            block()
        }
    }
}
