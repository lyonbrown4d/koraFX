@file:JvmName("NavigationComponentsKt")
@file:JvmMultifileClass

package dev.korafx.navigation

import dev.korafx.dsl.FragmentBuilder
import dev.korafx.dsl.fragment
import dev.korafx.dsl.state.collectLatestIn
import javafx.animation.Animation
import javafx.scene.Node
import javafx.scene.layout.Pane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class ContentTransitionHost {
    private var currentContentKey: Any? = null
    private var activeTransition: Animation? = null
    private var renderEpoch = 0

    fun render(
        host: Pane,
        nextNode: Node,
        transition: RouteTransition,
        onTransitionCompleted: () -> Unit = {},
    ) {
        render(host, nextNode, { host.children.setAll(nextNode) }, transition, onTransitionCompleted)
    }

    fun render(
        host: Pane,
        nextChildren: List<Node>,
        transition: RouteTransition,
        onTransitionCompleted: () -> Unit = {},
    ) {
        val key = nextChildren
        render(host, key, { host.children.setAll(nextChildren) }, transition, onTransitionCompleted)
    }

    private fun render(
        host: Pane,
        nextContentKey: Any,
        swapContent: () -> Unit,
        transition: RouteTransition,
        onTransitionCompleted: () -> Unit,
    ) {
        val epoch = ++renderEpoch

        if (currentContentKey === nextContentKey) {
            if (host.children.isEmpty()) {
                swapContent()
            }
            onTransitionCompleted()
            return
        }

        activeTransition?.stop()
        activeTransition = null
        transition.resetHost(host)

        if (host.children.isEmpty() || transition == RouteTransition.None) {
            swapContent()
            currentContentKey = nextContentKey
            transition.resetHost(host)
            onTransitionCompleted()
            return
        }

        val animation = transition.createAnimation(
            host = host,
            swapContent = swapContent,
            onFinished = {
                if (epoch == renderEpoch) {
                    currentContentKey = nextContentKey
                    transition.resetHost(host)
                    onTransitionCompleted()
                }
            },
        ) ?: run {
            swapContent()
            currentContentKey = nextContentKey
            transition.resetHost(host)
            onTransitionCompleted()
            return
        }

        activeTransition = animation
        animation.playFromStart()
    }
}

fun <T> Pane.bindContentWithTransition(
    scope: CoroutineScope,
    state: Flow<T>,
    transition: RouteTransition = RouteTransition.Fade(),
    content: FragmentBuilder.(T) -> Unit,
): Job {
    val transitionHost = ContentTransitionHost()
    return state.collectLatestIn(scope) {
        runNavigationOnFxThread {
            val nextChildren = fragment {
                content(it)
            }
            transitionHost.render(this, nextChildren, transition)
        }
    }
}

fun <T> Pane.bindContentWithTransition(
    scope: CoroutineScope,
    state: Flow<T>,
    transition: Flow<RouteTransition>,
    content: FragmentBuilder.(T) -> Unit,
): Job {
    val transitionHost = ContentTransitionHost()
    return state
        .combine(transition) { value, routeTransition -> value to routeTransition }
        .collectLatestIn(scope) { (value, routeTransition) ->
            runNavigationOnFxThread {
                val nextChildren = fragment {
                    content(value)
                }
                transitionHost.render(this, nextChildren, routeTransition)
            }
        }
}
