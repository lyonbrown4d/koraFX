package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClass
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
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

class InspectorPanelBuilder internal constructor(
    private val panel: InspectorPanel,
) {
    fun title(text: String?) {
        panel.setTitle(text)
    }

    fun subtitle(text: String?) {
        panel.setSubtitle(text)
    }

    fun emptyState(text: String) {
        panel.showEmpty(text)
    }

    fun metadata(content: InspectorMetadataBuilder.() -> Unit) {
        InspectorMetadataBuilder(panel).content()
    }

    fun badge(
        text: String,
        tone: ComponentTone = ComponentTone.NEUTRAL,
    ): Label =
        panel.addMetadata(dev.korafx.components.badge(text, tone)) as Label

    fun property(
        name: String,
        value: String?,
    ): HBox =
        panel.addProperty(name, value)

    fun property(
        name: String,
        value: Node,
    ): HBox =
        panel.addProperty(name, value)

    fun section(
        title: String,
        content: InspectorSectionBuilder.() -> Unit = {},
    ): VBox =
        panel.addSection(title, content)

    fun node(node: Node): Node =
        panel.addBodyNode(node)

    fun actions(content: InspectorActionsBuilder.() -> Unit) {
        InspectorActionsBuilder(panel).content()
    }
}

class InspectorSectionBuilder internal constructor(
    private val section: VBox,
) {
    fun property(
        name: String,
        value: String?,
    ): HBox =
        property(name, Label(value.orEmpty()).apply {
            isWrapText = true
            maxWidth = Double.MAX_VALUE
        })

    fun property(
        name: String,
        value: Node,
    ): HBox =
        inspectorPropertyRow(name, value).also {
            section.children += it
        }

    fun node(node: Node): Node =
        node.apply {
            styleClass("inspector-panel-section-content")
            section.children += this
        }

    fun badge(
        text: String,
        tone: ComponentTone = ComponentTone.NEUTRAL,
    ): Label =
        node(dev.korafx.components.badge(text, tone)) as Label
}

class InspectorMetadataBuilder internal constructor(
    private val panel: InspectorPanel,
) {
    fun node(node: Node): Node =
        panel.addMetadata(node)

    fun badge(
        text: String,
        tone: ComponentTone = ComponentTone.NEUTRAL,
    ): Label =
        node(dev.korafx.components.badge(text, tone)) as Label
}

class InspectorActionsBuilder internal constructor(
    private val panel: InspectorPanel,
) {
    fun node(node: Node): Node =
        panel.addAction(node)

    fun action(
        text: String,
        init: Button.() -> Unit = {},
        handler: () -> Unit,
    ): Button =
        Button(text).apply {
            init()
            onAction {
                handler()
            }
        }.also {
            panel.addAction(it)
        }

    fun spacer(): Region =
        Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }.also {
            panel.addAction(it)
        }
}

fun inspectorPanel(
    title: String? = null,
    subtitle: String? = null,
    emptyText: String? = null,
    init: InspectorPanel.() -> Unit = {},
    content: InspectorPanelBuilder.() -> Unit = {},
): InspectorPanel =
    InspectorPanel(
        title = title,
        subtitle = subtitle,
        emptyText = emptyText,
    ).apply(init).apply {
        InspectorPanelBuilder(this).content()
    }

fun NodeContainerBuilder.inspectorPanel(
    title: String? = null,
    subtitle: String? = null,
    emptyText: String? = null,
    init: InspectorPanel.() -> Unit = {},
    content: InspectorPanelBuilder.() -> Unit = {},
): InspectorPanel =
    add(
        dev.korafx.components.inspectorPanel(
            title = title,
            subtitle = subtitle,
            emptyText = emptyText,
            init = init,
            content = content,
        ),
    )

private fun inspectorPropertyRow(
    name: String,
    value: Node,
): HBox =
    HBox(10.0).apply {
        styleClass("inspector-panel-property")
        alignment = Pos.TOP_LEFT
        children += Label(name).apply {
            styleClass("inspector-panel-property-name")
            isWrapText = true
        }
        children += value.apply {
            styleClass("inspector-panel-property-value")
            HBox.setHgrow(this, Priority.ALWAYS)
        }
    }
