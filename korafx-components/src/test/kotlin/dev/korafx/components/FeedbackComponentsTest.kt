package dev.korafx.components

import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FeedbackComponentsTest {
    @Test
    fun `empty state renders title message and action`() {
        FxTestSupport.runOnFxThread {
            var actionCount = 0
            val state = emptyState(
                title = "No files",
                message = "Drop files here to import.",
                actionText = "Browse",
                onAction = { actionCount += 1 },
            )

            val labels = state.children.filterIsInstance<Label>()
            val button = state.children
                .flatMap { child -> (child as? javafx.scene.layout.HBox)?.children ?: emptyList() }
                .filterIsInstance<Button>()
                .single()

            assertTrue("feedback-state" in state.styleClass)
            assertTrue("empty-state" in state.styleClass)
            assertEquals("No files", labels[0].text)
            assertEquals("Drop files here to import.", labels[1].text)

            button.fire()
            assertEquals(1, actionCount)
        }
    }

    @Test
    fun `loading state renders progress indicator`() {
        FxTestSupport.runOnFxThread {
            val state = loadingState(message = "Loading routes", progress = 0.5)

            assertTrue("feedback-state" in state.styleClass)
            assertTrue("loading-state" in state.styleClass)
            assertIs<ProgressIndicator>(state.children.first())
            assertEquals(0.5, (state.children.first() as ProgressIndicator).progress)
            assertEquals("Loading routes", state.children.filterIsInstance<Label>().single().text)
        }
    }

    @Test
    fun `error state renders semantic classes`() {
        FxTestSupport.runOnFxThread {
            val state = errorState(
                title = "Load failed",
                message = "Try again later.",
            )

            val labels = state.children.filterIsInstance<Label>()

            assertTrue("feedback-state" in state.styleClass)
            assertTrue("error-state" in state.styleClass)
            assertEquals("Load failed", labels[0].text)
            assertEquals("Try again later.", labels[1].text)
        }
    }

    @Test
    fun `feedback components can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                emptyState("Empty")
                loadingState("Loading")
                errorState("Error")
            }

            assertEquals(3, root.children.size)
            assertTrue("empty-state" in root.children[0].styleClass)
            assertTrue("loading-state" in root.children[1].styleClass)
            assertTrue("error-state" in root.children[2].styleClass)
        }
    }
}
