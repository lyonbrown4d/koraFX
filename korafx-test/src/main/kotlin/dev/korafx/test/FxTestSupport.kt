package dev.korafx.test

import javafx.application.Platform
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.testfx.api.FxRobot
import java.util.concurrent.ExecutionException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference

object FxTestSupport {
    private val started = AtomicReference(false)

    fun start() {
        startToolkit()
    }

    fun runOnFxThread(block: () -> Unit) {
        callOnFxThread {
            block()
        }
    }

    fun <T> callOnFxThread(block: () -> T): T {
        startToolkit()

        if (Platform.isFxApplicationThread()) {
            return block()
        }

        val task = FutureTask(block)
        Platform.runLater(task)

        return try {
            task.get(5, TimeUnit.SECONDS)
        } catch (_: TimeoutException) {
            error("Timed out waiting for JavaFX test action.")
        } catch (error: ExecutionException) {
            throw error.cause ?: error
        }
    }

    fun waitForFxCondition(
        timeoutMillis: Long = 5_000,
        pollMillis: Long = 10,
        condition: () -> Boolean,
    ) {
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis)
        while (System.nanoTime() < deadline) {
            var matched = false
            runOnFxThread {
                matched = condition()
            }

            if (matched) {
                return
            }

            Thread.sleep(pollMillis)
        }

        error("Timed out waiting for JavaFX condition.")
    }

    fun showStage(
        root: Parent,
        width: Double = 800.0,
        height: Double = 600.0,
        title: String = "KoraFX Test",
    ): Stage =
        callOnFxThread {
            Stage().apply {
                this.title = title
                scene = Scene(root, width, height)
                show()
            }
        }

    fun showStage(
        width: Double = 800.0,
        height: Double = 600.0,
        title: String = "KoraFX Test",
        root: () -> Parent,
    ): Stage =
        callOnFxThread {
            Stage().apply {
                this.title = title
                scene = Scene(root(), width, height)
                show()
            }
        }

    private fun startToolkit() {
        if (started.get()) {
            return
        }

        val latch = CountDownLatch(1)
        try {
            Platform.startup {
                Platform.setImplicitExit(false)
                latch.countDown()
            }
            check(latch.await(5, TimeUnit.SECONDS)) {
                "Timed out starting JavaFX toolkit."
            }
        } catch (_: IllegalStateException) {
            // JavaFX toolkit is already running in this JVM.
            Platform.setImplicitExit(false)
        }
        started.set(true)
    }
}

fun FxRobot.runOnFxThread(block: () -> Unit): FxRobot =
    apply {
        interact(block)
    }

fun FxRobot.waitForFxCondition(
    timeoutMillis: Long = 5_000,
    pollMillis: Long = 10,
    condition: () -> Boolean,
): FxRobot =
    apply {
        FxTestSupport.waitForFxCondition(
            timeoutMillis = timeoutMillis,
            pollMillis = pollMillis,
            condition = condition,
        )
    }

fun Stage.setTestScene(
    root: Parent,
    width: Double = 800.0,
    height: Double = 600.0,
): Stage =
    apply {
        scene = Scene(root, width, height)
    }
