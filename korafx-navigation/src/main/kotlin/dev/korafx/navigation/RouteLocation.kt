package dev.korafx.navigation

data class RouteMeta(
    val values: Map<String, Any?> = emptyMap(),
) {
    operator fun get(key: String): Any? = values[key]

    fun boolean(
        key: String,
        defaultValue: Boolean = false,
    ): Boolean = values[key] as? Boolean ?: defaultValue

    fun string(key: String): String? = values[key] as? String

    operator fun plus(other: RouteMeta): RouteMeta =
        RouteMeta(values + other.values)

    companion object {
        val Empty: RouteMeta = RouteMeta()
    }
}

fun routeMeta(vararg values: Pair<String, Any?>): RouteMeta =
    RouteMeta(values.toMap())

interface PathRoute : Route {
    val path: String
        get() = id

    val meta: RouteMeta
        get() = RouteMeta.Empty
}

data class RouteQuery(
    val values: Map<String, List<String>> = emptyMap(),
) {
    operator fun get(key: String): String? = values[key]?.firstOrNull()

    fun all(key: String): List<String> = values[key].orEmpty()

    fun required(key: String): String =
        get(key) ?: error("Missing required route query '$key'.")

    fun int(key: String): Int? = get(key)?.toIntOrNull()

    fun long(key: String): Long? = get(key)?.toLongOrNull()

    fun boolean(key: String): Boolean? =
        when (get(key)?.lowercase()) {
            "true", "1", "yes", "on" -> true
            "false", "0", "no", "off" -> false
            else -> null
        }

    fun with(values: Map<String, Any?>): RouteQuery =
        RouteQuery(this.values + values.toRouteQueryValues())

    fun without(vararg keys: String): RouteQuery =
        RouteQuery(values - keys.toSet())

    fun asQueryMap(): Map<String, List<String>> = values

    companion object {
        val Empty: RouteQuery = RouteQuery()
    }
}

data class NavigationLocation<R : Route>(
    val route: R,
    val path: String,
    val fullPath: String,
    val params: Map<String, String> = emptyMap(),
    val query: RouteQuery = RouteQuery.Empty,
    val hash: String? = null,
    val meta: RouteMeta = RouteMeta.Empty,
) {
    val routeId: String
        get() = route.id

    fun param(key: String): String? = params[key]

    fun requiredParam(key: String): String =
        params[key] ?: error("Missing required route param '$key'.")

    fun intParam(key: String): Int? = param(key)?.toIntOrNull()

    fun longParam(key: String): Long? = param(key)?.toLongOrNull()

    fun withQuery(
        vararg values: Pair<String, Any?>,
        preserveExisting: Boolean = true,
    ): String = withQuery(values.toMap(), preserveExisting)

    fun withQuery(
        values: Map<String, Any?>,
        preserveExisting: Boolean = true,
    ): String {
        val nextQuery =
            if (preserveExisting) {
                query.with(values).asQueryMap()
            } else {
                values.toRouteQueryValues()
            }
        return RoutePattern.build(
            pattern = path,
            query = nextQuery,
            hash = hash,
        )
    }

    fun withoutQuery(vararg keys: String): String =
        RoutePattern.build(
            pattern = path,
            query = query.without(*keys).asQueryMap(),
            hash = hash,
        )

    fun withHash(hash: String?): String =
        RoutePattern.build(
            pattern = path,
            query = query.asQueryMap(),
            hash = hash,
        )

    fun withoutHash(): String = withHash(null)
}

internal fun Route.routePath(): String =
    (this as? PathRoute)?.path ?: id

internal fun Route.routeMeta(): RouteMeta =
    (this as? PathRoute)?.meta ?: RouteMeta.Empty

fun PathRoute.location(
    params: Map<String, Any?> = emptyMap(),
    query: Map<String, Any?> = emptyMap(),
    hash: String? = null,
): String =
    RoutePattern.build(
        pattern = path,
        params = params,
        query = query,
        hash = hash,
    )

private fun Map<String, Any?>.toRouteQueryValues(): Map<String, List<String>> =
    mapNotNull { (key, value) ->
        when (value) {
            null -> null
            is Iterable<*> -> key to value.filterNotNull().map { item -> item.toString() }
            is Array<*> -> key to value.filterNotNull().map { item -> item.toString() }
            else -> key to listOf(value.toString())
        }
    }.toMap()
