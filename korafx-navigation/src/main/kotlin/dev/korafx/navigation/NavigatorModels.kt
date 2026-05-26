package dev.korafx.navigation

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
    val navigationType: NavigationType = NavigationType.INITIAL,
    val backStack: List<NavigationLocation<R>> = emptyList(),
    val forwardStack: List<NavigationLocation<R>> = emptyList(),
)

internal data class RouteMatchCandidate<R : Route>(
    val index: Int,
    val match: RouteMatch,
    val location: NavigationLocation<R>,
)
