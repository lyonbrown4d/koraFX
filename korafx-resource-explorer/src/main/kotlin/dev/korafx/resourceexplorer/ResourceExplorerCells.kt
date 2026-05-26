package dev.korafx.resourceexplorer

import dev.korafx.dsl.styleClass
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TreeCell
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

internal fun <T> ResourceExplorer<T>.installCellFactory() {
    treeView.setCellFactory {
        object : TreeCell<T>() {
            init {
                styleClass("resource-explorer-cell")
                setOnMouseClicked { event ->
                    val rowItem = item
                    if (
                        event.button == rowMouseButton &&
                        event.clickCount == rowClickCount &&
                        rowItem != null &&
                        !isEmpty
                    ) {
                        rowActionHandler?.invoke(rowItem)
                    }
                }
            }

            override fun updateItem(
                item: T?,
                empty: Boolean,
            ) {
                super.updateItem(item, empty)

                if (empty || item == null) {
                    text = null
                    graphic = null
                    contextMenu = null
                    return
                }

                updateCellContent(item)
                contextMenu = createContextMenu(item)
            }

            private fun updateCellContent(item: T) {
                val secondaryText = secondaryTextOf?.invoke(item)?.takeIf { it.isNotBlank() }
                val statusText = statusTextOf?.invoke(item)?.takeIf { it.isNotBlank() }

                if (secondaryText == null && statusText == null) {
                    text = textOf(item)
                    graphic = graphicOf?.invoke(item)
                    return
                }

                text = null
                graphic = createResourceRow(item, secondaryText, statusText)
            }
        }
    }
}

private fun <T> ResourceExplorer<T>.createResourceRow(
    item: T,
    secondaryText: String?,
    statusText: String?,
): Node =
    HBox(6.0).apply {
        styleClass("resource-explorer-row")
        alignment = Pos.CENTER_LEFT

        graphicOf?.invoke(item)?.let { icon ->
            icon.styleClass("resource-explorer-row-icon")
            children += icon
        }

        children += VBox(1.0).apply {
            styleClass("resource-explorer-row-text")
            children += Label(textOf(item)).apply {
                styleClass("resource-explorer-row-primary")
            }
            secondaryText?.let {
                children += Label(it).apply {
                    styleClass("resource-explorer-row-secondary")
                }
            }
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        statusText?.let {
            children += Label(it).apply {
                styleClass("resource-explorer-row-status")
            }
        }
    }
