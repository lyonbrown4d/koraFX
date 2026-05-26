@file:JvmName("NavigationComponentsKt")
@file:JvmMultifileClass

package dev.korafx.navigation

import dev.korafx.components.setKoraIcon
import dev.korafx.dsl.navButton
import dev.korafx.dsl.onAction
import dev.korafx.dsl.sidebar
import dev.korafx.dsl.state.collectLatestIn
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.kordamp.ikonli.Ikon

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
            runNavigationOnFxThread {
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
                                scope.launch {
                                    navigator.navigateAsync(route.id)
                                }
                            }
                            buttonInit(item)
                        }
                    },
                )
            }
        }
    }

fun <R : Route> routeButton(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    route: R,
    text: String = route.title,
    exact: Boolean = true,
    init: Button.() -> Unit = {},
    stateChanged: Button.(RouteLinkState<R>) -> Unit = {},
): Button =
    Button(text).apply {
        styleClass += "route-button"
        init()
        onAction {
            scope.launch {
                navigator.navigateAsync(route.id)
            }
        }
        bindRouteLinkState(
            scope = scope,
            navigator = navigator,
            activeClass = "route-button-active",
            inactiveClass = "route-button-inactive",
            active = { state ->
                if (exact) {
                    state.currentRoute.id == route.id
                } else {
                    state.currentLocation.route.id == route.id
                }
            },
            stateChanged = stateChanged,
        )
    }

fun <R : Route> pathButton(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    path: String,
    text: String = path,
    exact: Boolean = true,
    init: Button.() -> Unit = {},
    stateChanged: Button.(RouteLinkState<R>) -> Unit = {},
): Button =
    Button(text).apply {
        styleClass += "route-button"
        init()
        onAction {
            scope.launch {
                navigator.navigatePathAsync(path)
            }
        }
        bindRouteLinkState(
            scope = scope,
            navigator = navigator,
            activeClass = "route-button-active",
            inactiveClass = "route-button-inactive",
            active = { state -> state.currentLocation.isActivePath(path, exact) },
            stateChanged = stateChanged,
        )
    }

fun <R : Route> routeLink(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    route: R,
    text: String = route.title,
    exact: Boolean = true,
    init: Hyperlink.() -> Unit = {},
    stateChanged: Hyperlink.(RouteLinkState<R>) -> Unit = {},
): Hyperlink =
    Hyperlink(text).apply {
        styleClass += "route-link"
        init()
        onAction {
            scope.launch {
                navigator.navigateAsync(route.id)
            }
        }
        bindRouteLinkState(
            scope = scope,
            navigator = navigator,
            activeClass = "route-link-active",
            inactiveClass = "route-link-inactive",
            active = { state ->
                if (exact) {
                    state.currentRoute.id == route.id
                } else {
                    state.currentLocation.route.id == route.id
                }
            },
            stateChanged = stateChanged,
        )
    }

fun <R : Route> pathLink(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    path: String,
    text: String = path,
    exact: Boolean = true,
    init: Hyperlink.() -> Unit = {},
    stateChanged: Hyperlink.(RouteLinkState<R>) -> Unit = {},
): Hyperlink =
    Hyperlink(text).apply {
        styleClass += "route-link"
        init()
        onAction {
            scope.launch {
                navigator.navigatePathAsync(path)
            }
        }
        bindRouteLinkState(
            scope = scope,
            navigator = navigator,
            activeClass = "route-link-active",
            inactiveClass = "route-link-inactive",
            active = { state -> state.currentLocation.isActivePath(path, exact) },
            stateChanged = stateChanged,
        )
    }

private fun <R : Route, T : Node> T.bindRouteLinkState(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    activeClass: String,
    inactiveClass: String,
    active: (NavigationState<R>) -> Boolean,
    stateChanged: T.(RouteLinkState<R>) -> Unit,
) {
    navigator.state.collectLatestIn(scope) { state ->
        runNavigationOnFxThread {
            val isActive = active(state)
            updateActiveStyle(
                active = isActive,
                activeClass = activeClass,
                inactiveClass = inactiveClass,
            )
            stateChanged(
                RouteLinkState(
                    currentLocation = state.currentLocation,
                    active = isActive,
                ),
            )
        }
    }
}

private fun Node.updateActiveStyle(
    active: Boolean,
    activeClass: String,
    inactiveClass: String,
) {
    styleClass.removeAll(activeClass, inactiveClass)
    styleClass += if (active) activeClass else inactiveClass
}

private fun <R : Route> NavigationLocation<R>.isActivePath(
    path: String,
    exact: Boolean,
): Boolean {
    val target = RoutePattern.parseLocation(path)
    return if (exact) {
        fullPath == target.fullPath
    } else {
        this.path == target.path || this.path.startsWith("${target.path.trimEnd('/')}/")
    }
}
