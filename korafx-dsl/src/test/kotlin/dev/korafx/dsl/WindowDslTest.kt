package dev.korafx.dsl

import javafx.scene.layout.Pane
import javafx.stage.WindowEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class WindowDslTest {
    @Test
    fun `scene configures root size and initializer`() {
        FxTestSupport.runOnFxThread {
            val root = Pane()
            val scene = scene(root, width = 320.0, height = 240.0) {
                userData = "configured"
            }

            assertSame(root, scene.root)
            assertEquals(320.0, scene.width)
            assertEquals(240.0, scene.height)
            assertEquals("configured", scene.userData)
        }
    }

    @Test
    fun `stage configures title scene and hidden handler`() {
        FxTestSupport.runOnFxThread {
            var hidden = false
            val root = Pane()
            val stage = stage(title = "DevTools") {
                scene(width = 640.0, height = 480.0) {
                    root
                }
                onHidden {
                    hidden = true
                }
            }

            assertEquals("DevTools", stage.title)
            assertSame(root, stage.scene.root)
            assertEquals(640.0, stage.scene.width)
            assertEquals(480.0, stage.scene.height)

            stage.onHidden.handle(WindowEvent(stage, WindowEvent.WINDOW_HIDDEN))

            assertTrue(hidden)
        }
    }

    @Test
    fun `popup configures behavior and content`() {
        FxTestSupport.runOnFxThread {
            val root = Pane()
            val popup = popup(
                autoFix = false,
                autoHide = true,
                hideOnEscape = false,
            ) {
                add(root)
            }

            assertEquals(false, popup.isAutoFix)
            assertEquals(true, popup.isAutoHide)
            assertEquals(false, popup.isHideOnEscape)
            assertEquals(1, popup.content.size)
            assertSame(root, popup.content.single())
        }
    }
}
