package dev.korafx.devtools

import dev.korafx.dsl.rectangle
import dev.korafx.dsl.stackPane
import dev.korafx.dsl.styleClass
import javafx.scene.layout.StackPane

internal fun createDevtoolsInspectedHost(): StackPane =
    stackPane(
        init = {
            styleClass("korafx-devtools-inspected-host")
            isPickOnBounds = false
            val host = this
            clip = rectangle {
                widthProperty().bind(host.widthProperty())
                heightProperty().bind(host.heightProperty())
            }
        },
    ) {}
