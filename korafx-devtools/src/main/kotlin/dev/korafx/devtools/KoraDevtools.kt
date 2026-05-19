package dev.korafx.devtools

import dev.korafx.framework.KoraApplicationBuilder
import javafx.scene.input.KeyCombination

fun KoraApplicationBuilder.devtools(configure: KoraDevtoolsBuilder.() -> Unit = {}) {
    install(KoraDevtoolsPlugin(KoraDevtoolsBuilder().apply(configure).build()))
}

class KoraDevtoolsBuilder {
    var enabled: Boolean = true
    var title: String = "KoraFX DevTools"
    var shortcut: String = "Ctrl+Shift+I"
    var pickerShortcut: String = "Ctrl+Shift+C"
    var highlightSelection: Boolean = true
    var language: KoraDevtoolsLanguage = KoraDevtoolsLanguage.SYSTEM
    var placement: KoraDevtoolsPlacement = KoraDevtoolsPlacement.BOTTOM
    var dockHeight: Double = 360.0
    var width: Double = 1040.0
    var height: Double = 720.0
    private val panels = linkedSetOf<KoraDevtoolsPanel>()

    fun panels(configure: KoraDevtoolsPanelsBuilder.() -> Unit) {
        panels += KoraDevtoolsPanelsBuilder().apply(configure).build()
    }

    internal fun build(): KoraDevtoolsSpec =
        KoraDevtoolsSpec(
            enabled = enabled,
            title = title,
            shortcut = KeyCombination.keyCombination(shortcut),
            pickerShortcut = KeyCombination.keyCombination(pickerShortcut),
            highlightSelection = highlightSelection,
            language = language,
            placement = placement,
            dockHeight = dockHeight,
            width = width,
            height = height,
            panels = panels.ifEmpty {
                linkedSetOf(
                    KoraDevtoolsPanel.SceneGraph,
                    KoraDevtoolsPanel.Inspector,
                    KoraDevtoolsPanel.Navigation,
                    KoraDevtoolsPanel.Theme,
                )
            }.toList(),
        )
}

class KoraDevtoolsPanelsBuilder {
    private val panels = linkedSetOf<KoraDevtoolsPanel>()

    fun sceneGraph() {
        panels += KoraDevtoolsPanel.SceneGraph
    }

    fun inspector() {
        panels += KoraDevtoolsPanel.Inspector
    }

    fun navigation() {
        panels += KoraDevtoolsPanel.Navigation
    }

    fun theme() {
        panels += KoraDevtoolsPanel.Theme
    }

    internal fun build(): Set<KoraDevtoolsPanel> = panels
}

enum class KoraDevtoolsLanguage {
    SYSTEM,
    ENGLISH,
    CHINESE,
}

enum class KoraDevtoolsPlacement {
    BOTTOM,
    WINDOW,
}

sealed class KoraDevtoolsPanel(
    internal val title: String,
) {
    data object SceneGraph : KoraDevtoolsPanel("Scene Graph")
    data object Inspector : KoraDevtoolsPanel("Inspector")
    data object Navigation : KoraDevtoolsPanel("Navigation")
    data object Theme : KoraDevtoolsPanel("Theme")
}

internal data class KoraDevtoolsSpec(
    val enabled: Boolean,
    val title: String,
    val shortcut: KeyCombination,
    val pickerShortcut: KeyCombination,
    val highlightSelection: Boolean,
    val language: KoraDevtoolsLanguage,
    val placement: KoraDevtoolsPlacement,
    val dockHeight: Double,
    val width: Double,
    val height: Double,
    val panels: List<KoraDevtoolsPanel>,
)
