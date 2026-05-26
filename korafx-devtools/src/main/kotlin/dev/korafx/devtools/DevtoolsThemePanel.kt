package dev.korafx.devtools

import dev.korafx.dsl.borderPane
import dev.korafx.dsl.comboBox
import dev.korafx.dsl.textArea
import dev.korafx.framework.KoraApplication
import javafx.scene.Node
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal fun createDevtoolsThemePanel(
    app: KoraApplication,
    messages: DevtoolsMessages,
    jobSink: (Job) -> Unit,
): Node {
    var updating = false
    val combo = comboBox(app.themeManager.availableThemes) {
        render { theme ->
            "${theme.displayName} (${theme.id})"
        }
    }
    val details = textArea {
        isEditable = false
        isWrapText = false
    }

    combo.selectionModel.selectedItemProperty().addListener { _, _, theme ->
        if (theme != null && !updating) {
            app.themeManager.setTheme(theme)
            details.text = describeTheme(messages, theme)
        }
    }

    jobSink(
        app.uiScope.launch {
            app.themeManager.theme.collectLatest { theme ->
                updating = true
                combo.selectionModel.select(theme)
                details.text = describeTheme(messages, theme)
                updating = false
            }
        },
    )

    combo.selectionModel.select(app.themeManager.currentTheme())
    details.text = describeTheme(messages, app.themeManager.currentTheme())

    return borderPane {
        top {
            devtoolsToolbar(messages.activeTheme) {
                add(combo)
            }
        }
        center(details)
    }
}
