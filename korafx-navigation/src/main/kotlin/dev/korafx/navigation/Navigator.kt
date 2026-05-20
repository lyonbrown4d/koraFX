package dev.korafx.navigation

import dev.korafx.dsl.state.MutableStateStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

interface Route {
    val id: String
    val title: String
}

enum class PageInstancePolicy {
    RECREATE,
    KEEP_ALIVE,
    SINGLETON_IN_WINDOW,
}

data class NavigationState<R : Route>(
    val currentRoute: R,
    val routes: List<R>,
    val pageInstancePolicy: PageInstancePolicy,
    val currentLocation: NavigationLocation<R>,
    val previousLocation: NavigationLocation<R>? = null,
    val backStack: List<NavigationLocation<R>> = emptyList(),
    val forwardStack: List<NavigationLocation<R>> = emptyList(),
)

class Navigator<R : Route>(
    initialRoute: R,
    routes: List<R>,
    pageInstancePolicy: PageInstancePolicy = PageInstancePolicy.RECREATE,
) {
    private val routeList = routes.toList()
    private val guards = mutableListOf<NavigationGuard<R>>()
    private val suspendGuards = mutableListOf<SuspendNavigationGuard<R>>()
    private val enterGuards = linkedMapOf<String, MutableList<NavigationGuard<R>>>()
    private val leaveGuards = linkedMapOf<String, MutableList<NavigationGuard<R>>>()
    private val suspendEnterGuards = linkedMapOf<String, MutableList<SuspendNavigationGuard<R>>>()
    private val suspendLeaveGuards = linkedMapOf<String, MutableList<SuspendNavigationGuard<R>>>()
    @PublishedApi
    internal val restoredStates = linkedMapOf<String, MutableMap<String, Any?>>()
    private val resultStreams = linkedMapOf<String, MutableSharedFlow<Any?>>()
    private val stateStore = MutableStateStore(
        NavigationState(
            currentRoute = initialRoute,
            routes = routeList,
            pageInstancePolicy = pageInstancePolicy,
            currentLocation = initialRoute.toLocation(),
        ),
    )

    init {
        require(routeList.isNotEmpty()) {
            "Navigator requires at least one route."
        }
        require(routeList.map(Route::id).distinct().size == routeList.size) {
            "Navigator route ids must be unique."
        }
        require(routeList.any { it.id == initialRoute.id }) {
            "The initial route must exist in the navigator route list."
        }
    }

    val state: StateFlow<NavigationState<R>> = stateStore.state

    val currentRoute: R
        get() = stateStore.currentState.currentRoute

    val currentLocation: NavigationLocation<R>
        get() = stateStore.currentState.currentLocation

    val routes: List<R>
        get() = stateStore.currentState.routes

    val canGoBack: Boolean
        get() = stateStore.currentState.backStack.isNotEmpty()

    val canGoForward: Boolean
        get() = stateStore.currentState.forwardStack.isNotEmpty()

    fun beforeEach(guard: NavigationGuard<R>): AutoCloseable {
        guards += guard
        return AutoCloseable {
            guards -= guard
        }
    }

    fun beforeEachAsync(guard: SuspendNavigationGuard<R>): AutoCloseable {
        suspendGuards += guard
        return AutoCloseable {
            suspendGuards -= guard
        }
    }

    fun beforeEnter(
        route: R,
        guard: NavigationGuard<R>,
    ): AutoCloseable = beforeEnter(route.id, guard)

    fun beforeEnter(
        routeId: String,
        guard: NavigationGuard<R>,
    ): AutoCloseable = addRouteGuard(enterGuards, routeId, guard)

    fun beforeLeave(
        route: R,
        guard: NavigationGuard<R>,
    ): AutoCloseable = beforeLeave(route.id, guard)

    fun beforeLeave(
        routeId: String,
        guard: NavigationGuard<R>,
    ): AutoCloseable = addRouteGuard(leaveGuards, routeId, guard)

    fun beforeEnterAsync(
        route: R,
        guard: SuspendNavigationGuard<R>,
    ): AutoCloseable = beforeEnterAsync(route.id, guard)

    fun beforeEnterAsync(
        routeId: String,
        guard: SuspendNavigationGuard<R>,
    ): AutoCloseable = addRouteGuard(suspendEnterGuards, routeId, guard)

    fun beforeLeaveAsync(
        route: R,
        guard: SuspendNavigationGuard<R>,
    ): AutoCloseable = beforeLeaveAsync(route.id, guard)

    fun beforeLeaveAsync(
        routeId: String,
        guard: SuspendNavigationGuard<R>,
    ): AutoCloseable = addRouteGuard(suspendLeaveGuards, routeId, guard)

    fun saveState(
        key: String,
        value: Any?,
        location: NavigationLocation<R> = currentLocation,
    ) {
        require(key.isNotBlank()) {
            "Navigation state key cannot be blank."
        }
        restoredStates.getOrPut(location.fullPath) { linkedMapOf() }[key] = value
    }

    inline fun <reified T : Any> restoredState(
        key: String,
        location: NavigationLocation<R> = currentLocation,
    ): T? = restoredStates[location.fullPath]?.get(key) as? T

    fun clearRestoredState(location: NavigationLocation<R> = currentLocation) {
        restoredStates.remove(location.fullPath)
    }

    fun setResult(
        key: String,
        value: Any?,
    ) {
        resultStream(key).tryEmit(value)
    }

    fun <T : Any> setResult(
        key: NavigationResultKey<T>,
        value: T,
    ) {
        setResult(key.name, value)
    }

    inline fun <reified T : Any> results(key: String): Flow<T> =
        resultStream(key).mapNotNull { value -> value as? T }

    inline fun <reified T : Any> results(key: NavigationResultKey<T>): Flow<T> =
        results(key.name)

    suspend inline fun <reified T : Any> awaitResult(key: String): T =
        results<T>(key).first()

    suspend inline fun <reified T : Any> awaitResult(key: NavigationResultKey<T>): T =
        awaitResult(key.name)

    fun navigate(route: R) {
        navigateRoute(route, replace = false)
    }

    fun navigate(routeId: String): Boolean {
        val target = routes.firstOrNull { it.id == routeId } ?: return false
        return navigateRoute(target, replace = false)
    }

    fun navigatePath(path: String): Boolean {
        val target = matchPath(path) ?: return false
        return navigateToLocation(target, replace = false, type = NavigationType.PUSH)
    }

    suspend fun navigateAsync(route: R): Boolean =
        navigateRouteAsync(route, replace = false)

    suspend fun navigateAsync(routeId: String): Boolean {
        val target = routes.firstOrNull { it.id == routeId } ?: return false
        return navigateRouteAsync(target, replace = false)
    }

    suspend fun navigatePathAsync(path: String): Boolean {
        val target = matchPath(path) ?: return false
        return navigateToLocationAsync(target, replace = false, type = NavigationType.PUSH)
    }

    fun replace(route: R): Boolean =
        navigateRoute(route, replace = true)

    fun replace(routeId: String): Boolean {
        val target = routes.firstOrNull { it.id == routeId } ?: return false
        return navigateRoute(target, replace = true)
    }

    fun replacePath(path: String): Boolean {
        val target = matchPath(path) ?: return false
        return navigateToLocation(target, replace = true, type = NavigationType.REPLACE)
    }

    suspend fun replaceAsync(route: R): Boolean =
        navigateRouteAsync(route, replace = true)

    suspend fun replaceAsync(routeId: String): Boolean {
        val target = routes.firstOrNull { it.id == routeId } ?: return false
        return navigateRouteAsync(target, replace = true)
    }

    suspend fun replacePathAsync(path: String): Boolean {
        val target = matchPath(path) ?: return false
        return navigateToLocationAsync(target, replace = true, type = NavigationType.REPLACE)
    }

    fun back(): Boolean {
        val current = stateStore.currentState
        val target = current.backStack.lastOrNull() ?: return false
        return navigatePop(
            target = target,
            nextBackStack = current.backStack.dropLast(1),
            nextForwardStack = current.forwardStack + current.currentLocation,
        )
    }

    fun forward(): Boolean {
        val current = stateStore.currentState
        val target = current.forwardStack.lastOrNull() ?: return false
        return navigatePop(
            target = target,
            nextBackStack = current.backStack + current.currentLocation,
            nextForwardStack = current.forwardStack.dropLast(1),
        )
    }

    suspend fun backAsync(): Boolean {
        val current = stateStore.currentState
        val target = current.backStack.lastOrNull() ?: return false
        return navigatePopAsync(
            target = target,
            nextBackStack = current.backStack.dropLast(1),
            nextForwardStack = current.forwardStack + current.currentLocation,
        )
    }

    suspend fun forwardAsync(): Boolean {
        val current = stateStore.currentState
        val target = current.forwardStack.lastOrNull() ?: return false
        return navigatePopAsync(
            target = target,
            nextBackStack = current.backStack + current.currentLocation,
            nextForwardStack = current.forwardStack.dropLast(1),
        )
    }

    fun matchPath(path: String): NavigationLocation<R>? =
        routes
            .asSequence()
            .mapIndexedNotNull { index, route ->
                RoutePattern.match(route.routePath(), path)?.let { match ->
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

    private fun navigateRoute(
        route: R,
        replace: Boolean,
    ): Boolean {
        val target = routes.firstOrNull { it.id == route.id } ?: return false
        val type = if (replace) NavigationType.REPLACE else NavigationType.PUSH
        return navigateToLocation(target.toLocation(), replace = replace, type = type)
    }

    private suspend fun navigateRouteAsync(
        route: R,
        replace: Boolean,
    ): Boolean {
        val target = routes.firstOrNull { it.id == route.id } ?: return false
        val type = if (replace) NavigationType.REPLACE else NavigationType.PUSH
        return navigateToLocationAsync(target.toLocation(), replace = replace, type = type)
    }

    private fun navigateToLocation(
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
            is NavigationDecision.Redirect -> {
                return navigateRedirect(decision, redirectDepth + 1)
            }
        }

        stateStore.update { state ->
            state.copy(
                currentRoute = target.route,
                currentLocation = target,
                previousLocation = state.currentLocation,
                backStack = if (replace) state.backStack else state.backStack + state.currentLocation,
                forwardStack = emptyList(),
            )
        }
        return true
    }

    private fun navigatePop(
        target: NavigationLocation<R>,
        nextBackStack: List<NavigationLocation<R>>,
        nextForwardStack: List<NavigationLocation<R>>,
    ): Boolean {
        val current = stateStore.currentState
        when (val decision = guardDecision(current.currentLocation, target, NavigationType.POP)) {
            NavigationDecision.Allow -> Unit
            is NavigationDecision.Block -> return false
            is NavigationDecision.Redirect -> return navigateRedirect(decision, redirectDepth = 1)
        }

        stateStore.update { state ->
            state.copy(
                currentRoute = target.route,
                currentLocation = target,
                previousLocation = state.currentLocation,
                backStack = nextBackStack,
                forwardStack = nextForwardStack,
            )
        }
        return true
    }

    private suspend fun navigateToLocationAsync(
        target: NavigationLocation<R>,
        replace: Boolean,
        type: NavigationType,
        redirectDepth: Int = 0,
    ): Boolean {
        if (redirectDepth > MAX_REDIRECTS) {
            return false
        }

        val current = stateStore.currentState
        when (val decision = guardDecisionAsync(current.currentLocation, target, type)) {
            NavigationDecision.Allow -> Unit
            is NavigationDecision.Block -> return false
            is NavigationDecision.Redirect -> {
                return navigateRedirectAsync(decision, redirectDepth + 1)
            }
        }

        stateStore.update { state ->
            state.copy(
                currentRoute = target.route,
                currentLocation = target,
                previousLocation = state.currentLocation,
                backStack = if (replace) state.backStack else state.backStack + state.currentLocation,
                forwardStack = emptyList(),
            )
        }
        return true
    }

    private suspend fun navigatePopAsync(
        target: NavigationLocation<R>,
        nextBackStack: List<NavigationLocation<R>>,
        nextForwardStack: List<NavigationLocation<R>>,
    ): Boolean {
        val current = stateStore.currentState
        when (val decision = guardDecisionAsync(current.currentLocation, target, NavigationType.POP)) {
            NavigationDecision.Allow -> Unit
            is NavigationDecision.Block -> return false
            is NavigationDecision.Redirect -> return navigateRedirectAsync(decision, redirectDepth = 1)
        }

        stateStore.update { state ->
            state.copy(
                currentRoute = target.route,
                currentLocation = target,
                previousLocation = state.currentLocation,
                backStack = nextBackStack,
                forwardStack = nextForwardStack,
            )
        }
        return true
    }

    private fun guardDecision(
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

    private suspend fun guardDecisionAsync(
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

    private fun evaluateGuards(
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

    private suspend fun evaluateSuspendGuards(
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

    private fun navigateRedirect(
        decision: NavigationDecision.Redirect<R>,
        redirectDepth: Int,
    ): Boolean {
        if (redirectDepth > MAX_REDIRECTS) {
            return false
        }

        val target =
            decision.path?.let(::matchPath)
                ?: decision.routeId?.let { routeId -> routes.firstOrNull { it.id == routeId }?.toLocation() }
                ?: return false
        return navigateToLocation(
            target = target,
            replace = decision.replace,
            type = if (decision.replace) NavigationType.REPLACE else NavigationType.PUSH,
            redirectDepth = redirectDepth,
        )
    }

    private suspend fun navigateRedirectAsync(
        decision: NavigationDecision.Redirect<R>,
        redirectDepth: Int,
    ): Boolean {
        if (redirectDepth > MAX_REDIRECTS) {
            return false
        }

        val target =
            decision.path?.let(::matchPath)
                ?: decision.routeId?.let { routeId -> routes.firstOrNull { it.id == routeId }?.toLocation() }
                ?: return false
        return navigateToLocationAsync(
            target = target,
            replace = decision.replace,
            type = if (decision.replace) NavigationType.REPLACE else NavigationType.PUSH,
            redirectDepth = redirectDepth,
        )
    }

    private fun R.toLocation(): NavigationLocation<R> {
        val parsed = RoutePattern.parseLocation(routePath())
        return toLocation(parsed)
    }

    private fun R.toLocation(match: RouteMatch): NavigationLocation<R> =
        NavigationLocation(
            route = this,
            path = match.path,
            fullPath = match.fullPath,
            params = match.params,
            query = match.query,
            hash = match.hash,
            meta = routeMeta(),
        )

    @PublishedApi
    internal fun resultStream(key: String): MutableSharedFlow<Any?> {
        require(key.isNotBlank()) {
            "Navigation result key cannot be blank."
        }
        return resultStreams.getOrPut(key) {
            MutableSharedFlow(
                replay = 1,
                extraBufferCapacity = 16,
            )
        }
    }

    private fun requireRouteId(routeId: String) {
        require(routeList.any { route -> route.id == routeId }) {
            "Route guard references unknown route '$routeId'."
        }
    }

    private fun <G> addRouteGuard(
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

    companion object {
        private const val MAX_REDIRECTS = 8

        fun <R : Route> fromPath(
            initialPath: String,
            routes: List<R>,
            fallbackRoute: R? = null,
            pageInstancePolicy: PageInstancePolicy = PageInstancePolicy.RECREATE,
        ): Navigator<R> {
            val fallback = fallbackRoute ?: routes.firstOrNull()
                ?: error("Navigator requires at least one route.")
            return Navigator(
                initialRoute = fallback,
                routes = routes,
                pageInstancePolicy = pageInstancePolicy,
            ).also { navigator ->
                navigator.replacePath(initialPath)
            }
        }
    }
}

private data class RouteMatchCandidate<R : Route>(
    val index: Int,
    val match: RouteMatch,
    val location: NavigationLocation<R>,
)
