package dev.korafx.navigation

private const val MAX_ASYNC_REDIRECTS = 8

internal suspend fun <R : Route> Navigator<R>.navigateToLocationAsync(
    target: NavigationLocation<R>,
    replace: Boolean,
    type: NavigationType,
    redirectDepth: Int = 0,
): Boolean {
    if (redirectDepth > MAX_ASYNC_REDIRECTS) {
        return false
    }

    val current = stateStore.currentState
    when (val decision = guardDecisionAsync(current.currentLocation, target, type)) {
        NavigationDecision.Allow -> Unit
        is NavigationDecision.Block -> return false
        is NavigationDecision.Redirect -> return navigateRedirectAsync(decision, redirectDepth + 1)
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

internal suspend fun <R : Route> Navigator<R>.navigatePopAsync(
    target: NavigationLocation<R>,
    nextBackStack: List<NavigationLocation<R>>,
    nextForwardStack: List<NavigationLocation<R>>,
    type: NavigationType,
): Boolean {
    val current = stateStore.currentState
    when (val decision = guardDecisionAsync(current.currentLocation, target, type)) {
        NavigationDecision.Allow -> Unit
        is NavigationDecision.Block -> return false
        is NavigationDecision.Redirect -> return navigateRedirectAsync(decision, redirectDepth = 1)
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

internal fun <R : Route> Navigator<R>.navigateRedirect(
    decision: NavigationDecision.Redirect<R>,
    redirectDepth: Int,
): Boolean {
    if (redirectDepth > MAX_ASYNC_REDIRECTS) {
        return false
    }

    val target = decision.path?.let(::matchRoutePath)
        ?: decision.routeId?.let { routeId -> routeList.firstOrNull { it.id == routeId }?.toLocation(routePathById) }
        ?: return false
    return navigateRouteTarget(
        target = target,
        replace = decision.replace,
        type = if (decision.replace) NavigationType.REPLACE else NavigationType.PUSH,
        redirectDepth = redirectDepth,
    )
}

internal suspend fun <R : Route> Navigator<R>.navigateRedirectAsync(
    decision: NavigationDecision.Redirect<R>,
    redirectDepth: Int,
): Boolean {
    if (redirectDepth > MAX_ASYNC_REDIRECTS) {
        return false
    }

    val target = decision.path?.let(::matchRoutePath)
        ?: decision.routeId?.let { routeId -> routeList.firstOrNull { it.id == routeId }?.toLocation(routePathById) }
        ?: return false
    return navigateToLocationAsync(
        target = target,
        replace = decision.replace,
        type = if (decision.replace) NavigationType.REPLACE else NavigationType.PUSH,
        redirectDepth = redirectDepth,
    )
}

private fun <R : Route> Navigator<R>.navigateRouteTarget(
    target: NavigationLocation<R>,
    replace: Boolean,
    type: NavigationType,
    redirectDepth: Int,
): Boolean {
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
