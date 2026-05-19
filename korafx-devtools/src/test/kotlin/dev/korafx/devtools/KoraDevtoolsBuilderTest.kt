package dev.korafx.devtools

import javafx.scene.input.KeyCombination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class KoraDevtoolsBuilderTest {
    @Test
    fun `builder uses default panels`() {
        val spec = KoraDevtoolsBuilder().build()

        assertEquals(
            listOf(
                KoraDevtoolsPanel.SceneGraph,
                KoraDevtoolsPanel.Inspector,
                KoraDevtoolsPanel.Navigation,
                KoraDevtoolsPanel.Theme,
            ),
            spec.panels,
        )
        assertEquals("KoraFX DevTools", spec.title)
        assertEquals(KeyCombination.keyCombination("Ctrl+Shift+I"), spec.shortcut)
        assertEquals(KeyCombination.keyCombination("Ctrl+Shift+C"), spec.pickerShortcut)
        assertEquals(KoraDevtoolsLanguage.SYSTEM, spec.language)
        assertEquals(KoraDevtoolsPlacement.BOTTOM, spec.placement)
        assertEquals(360.0, spec.dockHeight)
    }

    @Test
    fun `builder accepts custom panels and disabled state`() {
        val spec = KoraDevtoolsBuilder().apply {
            enabled = false
            title = "Debug Tools"
            shortcut = "Alt+D"
            pickerShortcut = "Alt+P"
            highlightSelection = false
            language = KoraDevtoolsLanguage.CHINESE
            placement = KoraDevtoolsPlacement.WINDOW
            dockHeight = 420.0
            panels {
                theme()
                navigation()
            }
        }.build()

        assertFalse(spec.enabled)
        assertEquals("Debug Tools", spec.title)
        assertEquals(KeyCombination.keyCombination("Alt+D"), spec.shortcut)
        assertEquals(KeyCombination.keyCombination("Alt+P"), spec.pickerShortcut)
        assertFalse(spec.highlightSelection)
        assertEquals(KoraDevtoolsLanguage.CHINESE, spec.language)
        assertEquals(KoraDevtoolsPlacement.WINDOW, spec.placement)
        assertEquals(420.0, spec.dockHeight)
        assertEquals(listOf(KoraDevtoolsPanel.Theme, KoraDevtoolsPanel.Navigation), spec.panels)
    }
}
