@file:JvmName("NavigationComponentsKt")
@file:JvmMultifileClass

package dev.korafx.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val ROUTE_TRANSITION_META_KEY = "korafx.navigation.transition"
const val ROUTE_TRANSITION_PROFILE_META_KEY = "korafx.navigation.transitionProfile"

enum class NavigationTransitionProfile(
    val label: String,
    val resolve: (NavigationType) -> RouteTransition,
) {
    Adaptive("Adaptive", { type ->
        when (type) {
            NavigationType.INITIAL -> RouteTransition.None
            NavigationType.PUSH -> RouteTransition.Slide()
            NavigationType.REPLACE -> RouteTransition.Fade()
            NavigationType.POP -> RouteTransition.Slide(direction = RouteTransition.SlideDirection.START)
        }
    }),
    PushSlide("Push Slide", { RouteTransition.Slide() }),
    Fade("Fade", { RouteTransition.Fade() }),
    Scale("Scale", { RouteTransition.Scale() }),
    None("None", { RouteTransition.None }),
}

fun <R : Route> Flow<NavigationState<R>>.routeTransition(
    profile: NavigationTransitionProfile,
): Flow<RouteTransition> = map { state ->
    resolveRouteTransition(
        state.currentLocation.meta,
        state.navigationType,
        profile,
    )
}

private fun resolveRouteTransition(
    routeMeta: RouteMeta,
    navigationType: NavigationType,
    fallbackProfile: NavigationTransitionProfile,
): RouteTransition {
    parseRouteTransition(routeMeta[ROUTE_TRANSITION_META_KEY], navigationType)?.let { transition ->
        return transition
    }
    parseRouteTransition(routeMeta[ROUTE_TRANSITION_PROFILE_META_KEY], navigationType)?.let { transition ->
        return transition
    }
    return fallbackProfile.resolve(navigationType)
}

private fun parseRouteTransition(
    value: Any?,
    navigationType: NavigationType,
): RouteTransition? {
    return when (value) {
        is RouteTransition -> value
        is NavigationTransitionProfile -> value.resolve(navigationType)
        is String -> parseRouteTransitionFromString(value, navigationType)
        else -> null
    }
}

private fun parseRouteTransitionFromString(
    value: String,
    navigationType: NavigationType,
): RouteTransition? {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) {
        return null
    }

    if (trimmed.startsWith("profile:", ignoreCase = true)) {
        val profileName = trimmed.substringAfter(":").trim()
        return parseTransitionProfile(profileName)?.resolve(navigationType)
    }

    parseTransitionProfile(trimmed)?.let { return it.resolve(navigationType) }

    val parts = trimmed.split(":", limit = 2)
    val transitionName = parts.first().trim().lowercase()
    val arg = parts.getOrNull(1)?.trim()?.lowercase()

    return when (transitionName) {
        "none" -> RouteTransition.None
        "fade" -> RouteTransition.Fade()
        "slide" -> {
            if (arg == "start" || arg == "left" || arg == "rtl") {
                RouteTransition.Slide(direction = RouteTransition.SlideDirection.START)
            } else if (arg == "end" || arg == "right" || arg == "ltr") {
                RouteTransition.Slide(direction = RouteTransition.SlideDirection.END)
            } else {
                RouteTransition.Slide()
            }
        }

        "scale" -> RouteTransition.Scale()
        else -> null
    }
}

private fun parseTransitionProfile(value: String): NavigationTransitionProfile? {
    val normalized = value.trim()
    if (normalized.isEmpty()) {
        return null
    }
    val noSpace = normalized.replace(" ", "").lowercase()
    return NavigationTransitionProfile.entries.firstOrNull { profile ->
        profile.name.lowercase() == normalized.lowercase() ||
            profile.label.lowercase() == normalized.lowercase() ||
            profile.label.replace(" ", "").lowercase() == noSpace
    }
}
