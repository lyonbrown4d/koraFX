package dev.korafx.devtools

import dev.korafx.dsl.HBoxBuilder
import dev.korafx.dsl.hbox
import dev.korafx.dsl.label
import dev.korafx.dsl.paddingAll
import javafx.scene.Node

internal fun devtoolsToolbar(
    title: String,
    actions: HBoxBuilder.() -> Unit = {},
): Node =
    hbox(
        spacing = 8.0,
        init = {
            paddingAll(10.0)
        },
    ) {
        label(title)
        spacer()
        actions()
    }
