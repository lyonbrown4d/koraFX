package dev.korafx.dsl

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

abstract class StatefulPaneBuilder<S, T : Pane> internal constructor(
    scope: CoroutineScope,
    state: Flow<S>,
    protected val pane: T,
) : StatefulNodeContainerBuilder<S>(scope, state) {
    override fun append(node: Node) {
        pane.children += node
    }
}

class StatefulVBoxBuilder<S> internal constructor(
    scope: CoroutineScope,
    state: Flow<S>,
    box: VBox,
) : StatefulPaneBuilder<S, VBox>(scope, state, box) {
    fun alignment(value: Pos) {
        pane.alignment = value
    }

    fun fillWidth(value: Boolean = true) {
        pane.isFillWidth = value
    }

    fun spacing(value: Double) {
        pane.spacing = value
    }
}

class StatefulHBoxBuilder<S> internal constructor(
    scope: CoroutineScope,
    state: Flow<S>,
    box: HBox,
) : StatefulPaneBuilder<S, HBox>(scope, state, box) {
    fun alignment(value: Pos) {
        pane.alignment = value
    }

    fun fillHeight(value: Boolean = true) {
        pane.isFillHeight = value
    }

    fun spacing(value: Double) {
        pane.spacing = value
    }
}

class StatefulStackPaneBuilder<S> internal constructor(
    scope: CoroutineScope,
    state: Flow<S>,
    pane: StackPane,
) : StatefulPaneBuilder<S, StackPane>(scope, state, pane) {
    fun alignment(value: Pos) {
        pane.alignment = value
    }
}
