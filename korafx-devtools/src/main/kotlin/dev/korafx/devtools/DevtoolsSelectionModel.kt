package dev.korafx.devtools

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.scene.Node

internal class DevtoolsSelectionModel {
    val selectedNode = SimpleObjectProperty<Node?>()
    private val windowListeners = mutableListOf<ChangeListener<in Node?>>()

    fun select(node: Node?) {
        selectedNode.set(node)
    }

    fun clear() {
        selectedNode.set(null)
    }

    fun addWindowListener(listener: ChangeListener<in Node?>) {
        selectedNode.addListener(listener)
        windowListeners += listener
    }

    fun disposeWindowListeners() {
        windowListeners.forEach { listener ->
            selectedNode.removeListener(listener)
        }
        windowListeners.clear()
    }
}
