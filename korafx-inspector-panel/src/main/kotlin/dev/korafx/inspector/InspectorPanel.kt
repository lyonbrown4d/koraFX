package dev.korafx.inspector

import dev.korafx.dsl.styleClass
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class InspectorPanel internal constructor(
    title: String?,
    subtitle: String?,
    emptyText: String?,
) : VBox(12.0) {
    val header: VBox = VBox(6.0)
    val titleRow: HBox = HBox(8.0)
    val titleLabel: Label = Label()
    val subtitleLabel: Label = Label()
    val metadataBar: HBox = HBox(6.0)
    val body: VBox = VBox(10.0)
    val actions: HBox = HBox(8.0)
    val emptyLabel: Label = Label()

    private var defaultEmptyText: String? = emptyText

    init {
        styleClass("inspector-panel")
        maxWidth = Double.MAX_VALUE

        header.styleClass("inspector-panel-header")
        titleRow.styleClass("inspector-panel-title-row")
        titleRow.alignment = Pos.CENTER_LEFT
        titleLabel.styleClass("inspector-panel-title")
        subtitleLabel.styleClass("inspector-panel-subtitle")
        subtitleLabel.isWrapText = true
        metadataBar.styleClass("inspector-panel-metadata")
        metadataBar.alignment = Pos.CENTER_LEFT
        metadataBar.isVisible = false
        metadataBar.isManaged = false
        body.styleClass("inspector-panel-body")
        actions.styleClass("inspector-panel-actions")
        actions.alignment = Pos.CENTER_RIGHT
        actions.isVisible = false
        actions.isManaged = false
        emptyLabel.styleClass("inspector-panel-empty")
        emptyLabel.isWrapText = true

        titleRow.children += titleLabel
        header.children += titleRow
        header.children += subtitleLabel
        header.children += metadataBar
        children += header
        children += body
        children += actions

        setHeader(title, subtitle)
        if (emptyText != null) {
            showEmpty(emptyText)
        }
    }

    fun setHeader(
        title: String?,
        subtitle: String? = subtitleLabel.text.takeIf { subtitleLabel.isManaged },
    ) {
        setTitle(title)
        setSubtitle(subtitle)
        refreshHeaderVisibility()
    }

    fun setTitle(title: String?) {
        val visible = !title.isNullOrBlank()
        titleLabel.text = title.orEmpty()
        titleLabel.isVisible = visible
        titleLabel.isManaged = visible
        refreshHeaderVisibility()
    }

    fun setSubtitle(subtitle: String?) {
        val visible = !subtitle.isNullOrBlank()
        subtitleLabel.text = subtitle.orEmpty()
        subtitleLabel.isVisible = visible
        subtitleLabel.isManaged = visible
        refreshHeaderVisibility()
    }

    fun clearBody() {
        body.children.clear()
    }

    fun showEmpty(text: String? = defaultEmptyText) {
        defaultEmptyText = text
        clearBody()
        if (!text.isNullOrBlank()) {
            emptyLabel.text = text
            body.children += emptyLabel
        }
    }

    fun clearEmpty() {
        body.children.remove(emptyLabel)
    }

    fun addMetadata(node: Node): Node =
        node.also {
            it.styleClass("inspector-panel-metadata-item")
            metadataBar.children += it
            refreshMetadataVisibility()
            refreshHeaderVisibility()
        }

    fun clearMetadata() {
        metadataBar.children.clear()
        refreshMetadataVisibility()
        refreshHeaderVisibility()
    }

    fun addProperty(
        name: String,
        value: String?,
    ): HBox =
        addProperty(name, propertyValue(value))

    fun addProperty(
        name: String,
        value: Node,
    ): HBox {
        clearEmpty()
        val row = propertyRow(name, value)
        body.children += row
        return row
    }

    fun addSection(
        title: String,
        content: InspectorSectionBuilder.() -> Unit = {},
    ): VBox {
        clearEmpty()
        val section = VBox(8.0).apply {
            styleClass("inspector-panel-section")
            if (title.isNotBlank()) {
                children += Label(title).apply {
                    styleClass("inspector-panel-section-title")
                    isWrapText = true
                }
            }
        }
        InspectorSectionBuilder(section).content()
        body.children += section
        return section
    }

    fun addBodyNode(node: Node): Node {
        clearEmpty()
        node.styleClass("inspector-panel-content")
        body.children += node
        return node
    }

    fun addAction(node: Node): Node =
        node.also {
            it.styleClass("inspector-panel-action")
            actions.children += it
            refreshActionsVisibility()
        }

    fun clearActions() {
        actions.children.clear()
        refreshActionsVisibility()
    }

    internal fun propertyRow(
        name: String,
        value: Node,
    ): HBox =
        inspectorPropertyRow(name, value)

    private fun propertyValue(value: String?): Label =
        Label(value.orEmpty()).apply {
            isWrapText = true
            maxWidth = Double.MAX_VALUE
        }

    private fun refreshHeaderVisibility() {
        val visible =
            titleLabel.isManaged ||
                subtitleLabel.isManaged ||
                metadataBar.isManaged
        header.isVisible = visible
        header.isManaged = visible
        titleRow.isVisible = titleLabel.isManaged
        titleRow.isManaged = titleLabel.isManaged
    }

    private fun refreshMetadataVisibility() {
        val visible = metadataBar.children.isNotEmpty()
        metadataBar.isVisible = visible
        metadataBar.isManaged = visible
    }

    private fun refreshActionsVisibility() {
        val visible = actions.children.isNotEmpty()
        actions.isVisible = visible
        actions.isManaged = visible
    }
}
