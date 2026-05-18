package dev.korafx.components

import javafx.application.Platform
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

internal object FxTestSupport {
    private val started = AtomicReference(false)

    fun runOnFxThread(block: () -> Unit) {
        startToolkit()

        if (Platform.isFxApplicationThread()) {
            block()
            return
        }

        val failure = AtomicReference<Throwable?>(null)
        val latch = CountDownLatch(1)

        Platform.runLater {
            try {
                block()
            } catch (error: Throwable) {
                failure.set(error)
            } finally {
                latch.countDown()
            }
        }

        check(latch.await(5, TimeUnit.SECONDS)) {
            "Timed out waiting for JavaFX test action."
        }

        failure.get()?.let { throw it }
    }

    fun waitForFxCondition(
        timeoutMillis: Long = 5_000,
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

            Thread.sleep(10)
        }

        error("Timed out waiting for JavaFX condition.")
    }

    private fun startToolkit() {
        if (started.get()) {
            return
        }

        val latch = CountDownLatch(1)
        try {
            Platform.startup {
                latch.countDown()
            }
            check(latch.await(5, TimeUnit.SECONDS)) {
                "Timed out starting JavaFX toolkit."
            }
        } catch (_: IllegalStateException) {
            // JavaFX toolkit is already running in this JVM.
        }
        started.set(true)
    }
}
