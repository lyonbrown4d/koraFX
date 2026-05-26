package dev.korafx.navigation

enum class NavigationType {
    INITIAL,
    PUSH,
    REPLACE,
    POP,
}

data class NavigationGuardContext<R : Route>(
    val from: NavigationLocation<R>,
    val to: NavigationLocation<R>,
    val type: NavigationType,
)

sealed class NavigationDecision<out R : Route> {
    data object Allow : NavigationDecision<Nothing>()

    data class Block(
        val reason: String? = null,
    ) : NavigationDecision<Nothing>()

    data class Redirect<R : Route>(
        val routeId: String? = null,
        val path: String? = null,
        val replace: Boolean = true,
    ) : NavigationDecision<R>() {
        init {
            require(!routeId.isNullOrBlank() || !path.isNullOrBlank()) {
                "Navigation redirect requires either routeId or path."
            }
        }
    }
}

typealias NavigationGuard<R> = (NavigationGuardContext<R>) -> NavigationDecision<R>

typealias SuspendNavigationGuard<R> = suspend (NavigationGuardContext<R>) -> NavigationDecision<R>
