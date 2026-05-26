package dev.korafx.navigation

import javafx.application.Platform

internal fun runNavigationOnFxThread(block: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        block()
    } else {
        Platform.runLater {
            block()
        }
    }
}
