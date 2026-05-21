package dev.korafx.test

import javafx.application.Platform
import javafx.scene.control.Label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FxTestSupportTest {
    @Test
    fun `runs action on JavaFX thread`() {
        var ranOnFxThread = false

        FxTestSupport.runOnFxThread {
            ranOnFxThread = Platform.isFxApplicationThread()
        }

        assertTrue(ranOnFxThread)
    }

    @Test
    fun `creates test stage`() {
        val stage = FxTestSupport.showStage {
            Label("Ready")
        }

        try {
            FxTestSupport.runOnFxThread {
                assertEquals("KoraFX Test", stage.title)
                assertEquals("Ready", (stage.scene.root as Label).text)
            }
        } finally {
            FxTestSupport.runOnFxThread {
                stage.close()
            }
        }
    }
}
