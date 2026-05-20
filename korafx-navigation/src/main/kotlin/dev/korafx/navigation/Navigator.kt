package dev.korafx.navigation

import dev.korafx.dsl.state.MutableStateStore
import kotlinx.coroutines.flow.StateFlow

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
)

class Navigator<R : Route>(
    initialRoute: R,
    routes: List<R>,
    pageInstancePolicy: PageInstancePolicy = PageInstancePolicy.RECREATE,
) {
    private val routeList = routes.toList()
    private val stateStore = MutableStateStore(
        NavigationState(
            currentRoute = initialRoute,
            routes = routeList,
            pageInstancePolicy = pageInstancePolicy,
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

    val routes: List<R>
        get() = stateStore.currentState.routes

    fun navigate(route: R) {
        val target = routes.firstOrNull { it.id == route.id } ?: return
        stateStore.update { it.copy(currentRoute = target) }
    }

    fun navigate(routeId: String): Boolean {
        val target = routes.firstOrNull { it.id == routeId } ?: return false
        stateStore.update { it.copy(currentRoute = target) }
        return true
    }
}
