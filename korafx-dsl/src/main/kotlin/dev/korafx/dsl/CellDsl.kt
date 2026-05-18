package dev.korafx.dsl

import javafx.scene.layout.StackPane

class CellContentBuilder internal constructor(
    pane: StackPane,
) : PaneBuilder<StackPane>(pane)
