package dev.korafx.navigation

internal fun <R : Route> Navigator<R>.matchRoutePath(path: String): NavigationLocation<R>? =
    routeList
        .asSequence()
        .mapIndexedNotNull { index, route ->
            RoutePattern.match(route.compiledPath(routePathById), path)?.let { match ->
                RouteMatchCandidate(
                    index = index,
                    match = match,
                    location = route.toLocation(match),
                )
            }
        }
        .maxWithOrNull(
            compareBy<RouteMatchCandidate<R>> { candidate -> candidate.match.score }
                .thenByDescending { candidate -> candidate.index },
        )
        ?.location

internal fun <R : Route> R.toLocation(routePathById: Map<String, String>): NavigationLocation<R> {
    val parsed = RoutePattern.parseLocation(compiledPath(routePathById))
    return toLocation(parsed)
}

internal fun <R : Route> R.compiledPath(routePathById: Map<String, String>): String =
    if (this is PathRoute) routePathById[id] ?: routePath() else routePath()

internal fun <R : Route> R.toLocation(match: RouteMatch): NavigationLocation<R> =
    NavigationLocation(
        route = this,
        path = match.path,
        fullPath = match.fullPath,
        params = match.params,
        query = match.query,
        hash = match.hash,
        meta = routeMeta(),
    )

internal fun <R : Route> validateNestedRoutes(routes: Map<String, R>) {
    routes.values.forEach { route ->
        val nested = route as? NestedPathRoute ?: return@forEach
        val parent = nested.parentRouteId ?: return@forEach
        require(parent in routes) {
            "Nested route '${route.id}' references unknown parent '$parent'."
        }
    }
}

internal fun <R : Route> resolveRoutePaths(routes: Map<String, R>): Map<String, String> {
    val cache = linkedMapOf<String, String>()
    val visiting = linkedSetOf<String>()

    fun resolve(routeId: String): String {
        cache[routeId]?.let { return it }
        val route = routes[routeId] ?: error("Route '$routeId' is not registered.")
        if (!visiting.add(route.id)) {
            error("Detected circular nested route hierarchy at '${route.id}'.")
        }

        val resolved = when (route) {
            is NestedPathRoute -> resolveNestedRoutePath(route, routes, ::resolve)
            else -> (route as? PathRoute)?.path ?: route.id
        }

        visiting.remove(route.id)
        cache[route.id] = resolved
        return resolved
    }

    routes.keys.forEach(::resolve)
    return cache.toMap()
}

private fun <R : Route> resolveNestedRoutePath(
    route: NestedPathRoute,
    routes: Map<String, R>,
    resolve: (String) -> String,
): String {
    val ownPath = route.path
    val parentId = route.parentRouteId
    if (parentId == null) {
        return RoutePattern.normalize(ownPath)
    }
    if (route.isIndexRoute) {
        return resolve(parentId)
    }

    val parentRoute = routes[parentId]
        ?: error("Nested route '${route.id}' references unknown parent '$parentId'.")
    val parentPath = resolve(parentRoute.id)
    val normalizedParent = RoutePattern.normalize(parentPath)
    return if (ownPath.startsWith("/")) {
        RoutePattern.normalize(ownPath)
    } else {
        val segment = ownPath.trim().trimStart('/').trimEnd('/')
        when {
            segment.isBlank() -> parentPath
            normalizedParent == "/" -> "/$segment"
            else -> "$normalizedParent/$segment"
        }
    }
}
