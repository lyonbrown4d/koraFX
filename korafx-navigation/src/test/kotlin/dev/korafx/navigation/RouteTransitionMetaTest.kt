package dev.korafx.navigation

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class RouteTransitionMetaTest {
    @Test
    fun `route transition resolves route meta transition override`() {
        val navigator = Navigator(
            initialRoute = TransitionMetaRoute.Source,
            routes = TransitionMetaRoute.all,
        )
        val transitions = runBlocking {
            val collected = async {
                navigator.state.routeTransition(NavigationTransitionProfile.Scale).take(2).toList()
            }
            navigator.navigate(TransitionMetaRoute.CustomTransition.id)
            collected.await()
        }

        assertTrue(transitions[0] is RouteTransition.Scale)
        assertTrue(transitions[1] is RouteTransition.Fade)
    }
}
