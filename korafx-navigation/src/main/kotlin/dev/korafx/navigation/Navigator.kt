package dev.korafx.navigation

import dev.korafx.dsl.state.MutableStateStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

class Navigator<R : Route>(
    initialRoute: R,
    routes: List<R>,
    pageInstancePolicy: PageInstancePolicy = PageInstancePolicy.RECREATE,
) {
    internal val routeList = routes.toList()
    internal val routeById = routeList.associateBy(Route::id)
    internal val routePathById = resolveRoutePaths(routeById)
    internal val guards = mutableListOf<NavigationGuard<R>>()
    internal val suspendGuards = mutableListOf<SuspendNavigationGuard<R>>()
    internal val enterGuards = linkedMapOf<String, MutableList<NavigationGuard<R>>>()
    internal val leaveGuards = linkedMapOf<String, MutableList<NavigationGuard<R>>>()
    internal val suspendEnterGuards = linkedMapOf<String, MutableList<SuspendNavigationGuard<R>>>()
    internal val suspendLeaveGuards = linkedMapOf<String, MutableList<SuspendNavigationGuard<R>>>()

    @PublishedApi
    internal val restoredStates = linkedMapOf<String, MutableMap<String, Any?>>()

    private val resultStreams = linkedMapOf<String, MutableSharedFlow<Any?>>()

    internal val stateStore = MutableStateStore(
        NavigationState(
            currentRoute = initialRoute,
            routes = routeList,
            pageInstancePolicy = pageInstancePolicy,
            currentLocation = initialRoute.toLocation(routePathById),
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
        validateNestedRoutes(routeById)
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

    fun navigate(routeId: String): Boolean = navigateRouteId(routeId, replace = false)

    fun navigatePath(path: String): Boolean = navigatePath(path, replace = false)

    suspend fun navigateAsync(route: R): Boolean = navigateRouteAsync(route, replace = false)

    suspend fun navigateAsync(routeId: String): Boolean = navigateRouteIdAsync(routeId, replace = false)

    suspend fun navigatePathAsync(path: String): Boolean = navigatePathAsync(path, replace = false)

    fun replace(route: R): Boolean = navigateRoute(route, replace = true)

    fun replace(routeId: String): Boolean = navigateRouteId(routeId, replace = true)

    fun replacePath(path: String): Boolean = navigatePath(path, replace = true)

    suspend fun replaceAsync(route: R): Boolean = navigateRouteAsync(route, replace = true)

    suspend fun replaceAsync(routeId: String): Boolean = navigateRouteIdAsync(routeId, replace = true)

    suspend fun replacePathAsync(path: String): Boolean = navigatePathAsync(path, replace = true)

    fun back(): Boolean = navigateBack()

    fun forward(): Boolean = navigateForward()

    suspend fun backAsync(): Boolean = navigateBackAsync()

    suspend fun forwardAsync(): Boolean = navigateForwardAsync()

    fun clearNavigationHistory() {
        stateStore.update { state ->
            state.copy(
                backStack = emptyList(),
                forwardStack = emptyList(),
                previousLocation = null,
                navigationType = NavigationType.REPLACE,
            )
        }
    }

    fun popToRoot(): Boolean = navigatePopToRoot()

    suspend fun popToRootAsync(): Boolean = navigatePopToRootAsync()

    fun canNavigate(route: R, replace: Boolean = false): NavigationDecision<R> =
        canNavigateRoute(route, replace)

    fun canNavigate(routeId: String, replace: Boolean = false): NavigationDecision<R> =
        routeList.firstOrNull { it.id == routeId }?.let { route ->
            canNavigate(route, replace)
        } ?: NavigationDecision.Block("Unknown route '$routeId'.")

    fun canNavigatePath(path: String, replace: Boolean = false): NavigationDecision<R>? =
        canNavigateRoutePath(path, replace)

    suspend fun canNavigateAsync(route: R, replace: Boolean = false): NavigationDecision<R> =
        canNavigateRouteAsync(route, replace)

    suspend fun canNavigateAsync(routeId: String, replace: Boolean = false): NavigationDecision<R> =
        routeList.firstOrNull { it.id == routeId }?.let { route ->
            canNavigateAsync(route, replace)
        } ?: NavigationDecision.Block("Unknown route '$routeId'.")

    suspend fun canNavigatePathAsync(path: String, replace: Boolean = false): NavigationDecision<R>? =
        canNavigateRoutePathAsync(path, replace)

    fun matchPath(path: String): NavigationLocation<R>? = matchRoutePath(path)

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

    companion object {
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
