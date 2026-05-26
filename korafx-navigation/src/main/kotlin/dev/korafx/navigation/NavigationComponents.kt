package dev.korafx.navigation

data class NavigationRailItem<R : Route>(
    val route: R,
    val active: Boolean,
)

data class RouteLinkState<R : Route>(
    val currentLocation: NavigationLocation<R>,
    val active: Boolean,
)
