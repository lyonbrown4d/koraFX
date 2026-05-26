package dev.korafx.navigation

private const val MAX_REDIRECTS = 8

internal fun <R : Route> Navigator<R>.navigateRouteId(
    routeId: String,
    replace: Boolean,
): Boolean {
    val target = routeList.firstOrNull { it.id == routeId } ?: return false
    return navigateRoute(target, replace)
}

internal suspend fun <R : Route> Navigator<R>.navigateRouteIdAsync(
    routeId: String,
    replace: Boolean,
): Boolean {
    val target = routeList.firstOrNull { it.id == routeId } ?: return false
    return navigateRouteAsync(target, replace)
}

internal fun <R : Route> Navigator<R>.navigatePath(
    path: String,
    replace: Boolean,
): Boolean {
    val target = matchRoutePath(path) ?: return false
    val type = if (replace) NavigationType.REPLACE else NavigationType.PUSH
    return navigateToLocation(target, replace = replace, type = type)
}

internal suspend fun <R : Route> Navigator<R>.navigatePathAsync(
    path: String,
    replace: Boolean,
): Boolean {
    val target = matchRoutePath(path) ?: return false
    val type = if (replace) NavigationType.REPLACE else NavigationType.PUSH
    return navigateToLocationAsync(target, replace = replace, type = type)
}

internal fun <R : Route> Navigator<R>.navigateBack(): Boolean {
    val current = stateStore.currentState
    val target = current.backStack.lastOrNull() ?: return false
    return navigatePop(
        target = target,
        nextBackStack = current.backStack.dropLast(1),
        nextForwardStack = current.forwardStack + current.currentLocation,
        type = NavigationType.POP,
    )
}

internal fun <R : Route> Navigator<R>.navigateForward(): Boolean {
    val current = stateStore.currentState
    val target = current.forwardStack.lastOrNull() ?: return false
    return navigatePop(
        target = target,
        nextBackStack = current.backStack + current.currentLocation,
        nextForwardStack = current.forwardStack.dropLast(1),
        type = NavigationType.POP,
    )
}

internal suspend fun <R : Route> Navigator<R>.navigateBackAsync(): Boolean {
    val current = stateStore.currentState
    val target = current.backStack.lastOrNull() ?: return false
    return navigatePopAsync(
        target = target,
        nextBackStack = current.backStack.dropLast(1),
        nextForwardStack = current.forwardStack + current.currentLocation,
        type = NavigationType.POP,
    )
}

internal suspend fun <R : Route> Navigator<R>.navigateForwardAsync(): Boolean {
    val current = stateStore.currentState
    val target = current.forwardStack.lastOrNull() ?: return false
    return navigatePopAsync(
        target = target,
        nextBackStack = current.backStack + current.currentLocation,
        nextForwardStack = current.forwardStack.dropLast(1),
        type = NavigationType.POP,
    )
}

internal fun <R : Route> Navigator<R>.navigatePopToRoot(): Boolean {
    var moved = false
    while (navigateBack()) {
        moved = true
    }
    return moved
}

internal suspend fun <R : Route> Navigator<R>.navigatePopToRootAsync(): Boolean {
    var moved = false
    while (navigateBackAsync()) {
        moved = true
    }
    return moved
}

internal fun <R : Route> Navigator<R>.canNavigateRoute(
    route: R,
    replace: Boolean,
): NavigationDecision<R> {
    val target = routeList.firstOrNull { it.id == route.id } ?: return NavigationDecision.Block(
        "Unknown route '${route.id}'.",
    )
    val type = if (replace) NavigationType.REPLACE else NavigationType.PUSH
    return guardDecision(stateStore.currentState.currentLocation, target.toLocation(routePathById), type)
}

internal fun <R : Route> Navigator<R>.canNavigateRoutePath(
    path: String,
    replace: Boolean,
): NavigationDecision<R>? {
    val target = matchRoutePath(path) ?: return null
    val type = if (replace) NavigationType.REPLACE else NavigationType.PUSH
    return guardDecision(stateStore.currentState.currentLocation, target, type)
}

internal suspend fun <R : Route> Navigator<R>.canNavigateRouteAsync(
    route: R,
    replace: Boolean,
): NavigationDecision<R> {
    val target = routeList.firstOrNull { it.id == route.id } ?: return NavigationDecision.Block(
        "Unknown route '${route.id}'.",
    )
    val type = if (replace) NavigationType.REPLACE else NavigationType.PUSH
    return guardDecisionAsync(stateStore.currentState.currentLocation, target.toLocation(routePathById), type)
}

internal suspend fun <R : Route> Navigator<R>.canNavigateRoutePathAsync(
    path: String,
    replace: Boolean,
): NavigationDecision<R>? {
    val target = matchRoutePath(path) ?: return null
    val type = if (replace) NavigationType.REPLACE else NavigationType.PUSH
    return guardDecisionAsync(stateStore.currentState.currentLocation, target, type)
}

internal fun <R : Route> Navigator<R>.navigateRoute(
    route: R,
    replace: Boolean,
): Boolean {
    val target = routeList.firstOrNull { it.id == route.id } ?: return false
    val type = if (replace) NavigationType.REPLACE else NavigationType.PUSH
    return navigateToLocation(target.toLocation(routePathById), replace = replace, type = type)
}

internal suspend fun <R : Route> Navigator<R>.navigateRouteAsync(
    route: R,
    replace: Boolean,
): Boolean {
    val target = routeList.firstOrNull { it.id == route.id } ?: return false
    val type = if (replace) NavigationType.REPLACE else NavigationType.PUSH
    return navigateToLocationAsync(target.toLocation(routePathById), replace = replace, type = type)
}

private fun <R : Route> Navigator<R>.navigateToLocation(
    target: NavigationLocation<R>,
    replace: Boolean,
    type: NavigationType,
    redirectDepth: Int = 0,
): Boolean {
    if (redirectDepth > MAX_REDIRECTS) {
        return false
    }

    val current = stateStore.currentState
    when (val decision = guardDecision(current.currentLocation, target, type)) {
        NavigationDecision.Allow -> Unit
        is NavigationDecision.Block -> return false
        is NavigationDecision.Redirect -> return navigateRedirect(decision, redirectDepth + 1)
    }

    stateStore.update { state ->
        state.copy(
            currentRoute = target.route,
            currentLocation = target,
            previousLocation = state.currentLocation,
            navigationType = type,
            backStack = if (replace) state.backStack else state.backStack + state.currentLocation,
            forwardStack = emptyList(),
        )
    }
    return true
}

private fun <R : Route> Navigator<R>.navigatePop(
    target: NavigationLocation<R>,
    nextBackStack: List<NavigationLocation<R>>,
    nextForwardStack: List<NavigationLocation<R>>,
    type: NavigationType,
): Boolean {
    val current = stateStore.currentState
    when (val decision = guardDecision(current.currentLocation, target, type)) {
        NavigationDecision.Allow -> Unit
        is NavigationDecision.Block -> return false
        is NavigationDecision.Redirect -> return navigateRedirect(decision, redirectDepth = 1)
    }

    stateStore.update { state ->
        state.copy(
            currentRoute = target.route,
            currentLocation = target,
            previousLocation = state.currentLocation,
            navigationType = type,
            backStack = nextBackStack,
            forwardStack = nextForwardStack,
        )
    }
    return true
}
