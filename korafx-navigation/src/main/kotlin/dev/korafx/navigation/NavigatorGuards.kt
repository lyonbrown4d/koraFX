package dev.korafx.navigation

internal fun <R : Route, G> Navigator<R>.addRouteGuard(
    store: MutableMap<String, MutableList<G>>,
    routeId: String,
    guard: G,
): AutoCloseable {
    requireRouteId(routeId)
    val routeGuards = store.getOrPut(routeId) { mutableListOf() }
    routeGuards += guard
    return AutoCloseable {
        routeGuards -= guard
        if (routeGuards.isEmpty()) {
            store -= routeId
        }
    }
}

internal fun <R : Route> Navigator<R>.guardDecision(
    from: NavigationLocation<R>,
    to: NavigationLocation<R>,
    type: NavigationType,
): NavigationDecision<R> {
    val context = NavigationGuardContext(
        from = from,
        to = to,
        type = type,
    )

    evaluateGuards(leaveGuards[from.route.id].orEmpty(), context)?.let { decision ->
        return decision
    }
    evaluateGuards(guards, context)?.let { decision ->
        return decision
    }
    evaluateGuards(enterGuards[to.route.id].orEmpty(), context)?.let { decision ->
        return decision
    }

    return NavigationDecision.Allow
}

internal suspend fun <R : Route> Navigator<R>.guardDecisionAsync(
    from: NavigationLocation<R>,
    to: NavigationLocation<R>,
    type: NavigationType,
): NavigationDecision<R> {
    val syncDecision = guardDecision(from, to, type)
    if (syncDecision !is NavigationDecision.Allow) {
        return syncDecision
    }

    val context = NavigationGuardContext(
        from = from,
        to = to,
        type = type,
    )

    evaluateSuspendGuards(suspendLeaveGuards[from.route.id].orEmpty(), context)?.let { decision ->
        return decision
    }
    evaluateSuspendGuards(suspendGuards, context)?.let { decision ->
        return decision
    }
    evaluateSuspendGuards(suspendEnterGuards[to.route.id].orEmpty(), context)?.let { decision ->
        return decision
    }

    return NavigationDecision.Allow
}

private fun <R : Route> evaluateGuards(
    routeGuards: List<NavigationGuard<R>>,
    context: NavigationGuardContext<R>,
): NavigationDecision<R>? {
    routeGuards.forEach { guard ->
        when (val decision = guard(context)) {
            NavigationDecision.Allow -> Unit
            is NavigationDecision.Block -> return decision
            is NavigationDecision.Redirect -> return decision
        }
    }

    return null
}

private suspend fun <R : Route> evaluateSuspendGuards(
    routeGuards: List<SuspendNavigationGuard<R>>,
    context: NavigationGuardContext<R>,
): NavigationDecision<R>? {
    routeGuards.forEach { guard ->
        when (val decision = guard(context)) {
            NavigationDecision.Allow -> Unit
            is NavigationDecision.Block -> return decision
            is NavigationDecision.Redirect -> return decision
        }
    }

    return null
}

private fun <R : Route> Navigator<R>.requireRouteId(routeId: String) {
    require(routeList.any { route -> route.id == routeId }) {
        "Route guard references unknown route '$routeId'."
    }
}
