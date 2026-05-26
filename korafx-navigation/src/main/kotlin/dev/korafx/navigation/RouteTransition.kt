@file:JvmName("NavigationComponentsKt")
@file:JvmMultifileClass

package dev.korafx.navigation

import javafx.animation.Animation
import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.ParallelTransition
import javafx.animation.ScaleTransition
import javafx.animation.TranslateTransition
import javafx.scene.layout.Pane
import javafx.util.Duration

sealed class RouteTransition(val durationMs: Double = 170.0) {
    abstract fun createAnimation(
        host: Pane,
        swapContent: () -> Unit,
        onFinished: () -> Unit,
    ): Animation?

    abstract fun withDuration(durationMs: Double): RouteTransition

    internal fun resetHost(host: Pane) {
        host.opacity = 1.0
        host.translateX = 0.0
        host.translateY = 0.0
        host.scaleX = 1.0
        host.scaleY = 1.0
    }

    object None : RouteTransition(0.0) {
        override fun createAnimation(
            host: Pane,
            swapContent: () -> Unit,
            onFinished: () -> Unit,
        ): Animation? = null

        override fun withDuration(durationMs: Double): RouteTransition = this
    }

    class Fade(durationMs: Double = 190.0) : RouteTransition(durationMs) {
        override fun withDuration(durationMs: Double): RouteTransition = Fade(durationMs.coerceAtLeast(20.0))

        override fun createAnimation(
            host: Pane,
            swapContent: () -> Unit,
            onFinished: () -> Unit,
        ): Animation {
            val fadeOut = FadeTransition(Duration.millis(durationMs), host).apply {
                fromValue = 1.0
                toValue = 0.0
                interpolator = Interpolator.EASE_BOTH
            }

            fadeOut.setOnFinished {
                swapContent()
                val fadeIn = FadeTransition(Duration.millis(durationMs), host).apply {
                    fromValue = 0.0
                    toValue = 1.0
                    interpolator = Interpolator.EASE_BOTH
                }
                fadeIn.setOnFinished { onFinished() }
                fadeIn.playFromStart()
            }

            return fadeOut
        }
    }

    class Slide(
        durationMs: Double = 210.0,
        private val distance: Double = 28.0,
        private val direction: SlideDirection = SlideDirection.END,
    ) : RouteTransition(durationMs) {
        override fun withDuration(durationMs: Double): RouteTransition = Slide(
            durationMs = durationMs.coerceAtLeast(20.0),
            distance = distance,
            direction = direction,
        )

        override fun createAnimation(
            host: Pane,
            swapContent: () -> Unit,
            onFinished: () -> Unit,
        ): Animation {
            val directionOffset = direction.offset * distance
            val fadeOut = FadeTransition(Duration.millis(durationMs), host).apply {
                fromValue = 1.0
                toValue = 0.45
            }
            val slideOut = TranslateTransition(Duration.millis(durationMs), host).apply {
                fromX = 0.0
                toX = directionOffset
            }
            val exit = ParallelTransition(fadeOut, slideOut)

            exit.setOnFinished {
                swapContent()
                host.translateX = -directionOffset
                host.opacity = 0.0

                val fadeIn = FadeTransition(Duration.millis(durationMs), host).apply {
                    fromValue = 0.0
                    toValue = 1.0
                    interpolator = Interpolator.EASE_BOTH
                }
                val slideIn = TranslateTransition(Duration.millis(durationMs), host).apply {
                    fromX = -directionOffset
                    toX = 0.0
                }
                val enter = ParallelTransition(fadeIn, slideIn)
                enter.setOnFinished {
                    onFinished()
                }
                enter.playFromStart()
            }

            return exit
        }
    }

    class Scale(
        durationMs: Double = 220.0,
        private val scaleFrom: Double = 0.985,
    ) : RouteTransition(durationMs) {
        override fun withDuration(durationMs: Double): RouteTransition = Scale(
            durationMs = durationMs.coerceAtLeast(20.0),
            scaleFrom = scaleFrom,
        )

        override fun createAnimation(
            host: Pane,
            swapContent: () -> Unit,
            onFinished: () -> Unit,
        ): Animation {
            val fadeOut = FadeTransition(Duration.millis(durationMs), host).apply {
                fromValue = 1.0
                toValue = 0.0
            }
            val scaleOutX = ScaleTransition(Duration.millis(durationMs), host).apply {
                fromX = 1.0
                toX = scaleFrom
            }
            val scaleOutY = ScaleTransition(Duration.millis(durationMs), host).apply {
                fromY = 1.0
                toY = scaleFrom
            }
            val exit = ParallelTransition(fadeOut, scaleOutX, scaleOutY)

            exit.setOnFinished {
                swapContent()
                host.scaleX = scaleFrom
                host.scaleY = scaleFrom
                host.opacity = 0.0

                val fadeIn = FadeTransition(Duration.millis(durationMs), host).apply {
                    fromValue = 0.0
                    toValue = 1.0
                }
                val scaleInX = ScaleTransition(Duration.millis(durationMs), host).apply {
                    fromX = scaleFrom
                    toX = 1.0
                }
                val scaleInY = ScaleTransition(Duration.millis(durationMs), host).apply {
                    fromY = scaleFrom
                    toY = 1.0
                }
                val enter = ParallelTransition(fadeIn, scaleInX, scaleInY)
                enter.setOnFinished {
                    onFinished()
                }
                enter.playFromStart()
            }

            return exit
        }
    }

    class Custom(
        private val factory: (Pane, () -> Unit, () -> Unit) -> Animation?,
    ) : RouteTransition(0.0) {
        override fun withDuration(durationMs: Double): RouteTransition = this

        override fun createAnimation(
            host: Pane,
            swapContent: () -> Unit,
            onFinished: () -> Unit,
        ): Animation? = factory(host, swapContent, onFinished)
    }

    enum class SlideDirection(val offset: Double) {
        START(-1.0),
        END(1.0),
    }
}

fun RouteTransition.scaleDuration(factor: Double): RouteTransition {
    val safeFactor = factor.coerceIn(0.1, 5.0)
    if (safeFactor == 1.0) {
        return this
    }

    return when (this) {
        is RouteTransition.Custom -> this
        is RouteTransition.None -> this
        else -> withDuration(durationMs * safeFactor)
    }
}
