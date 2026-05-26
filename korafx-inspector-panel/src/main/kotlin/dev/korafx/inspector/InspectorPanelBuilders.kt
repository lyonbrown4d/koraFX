package dev.korafx.inspector

import dev.korafx.components.ComponentTone
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
        dev.korafx.inspector.inspectorPanel(
            title = title,
            subtitle = subtitle,
            emptyText = emptyText,
            init = init,
            content = content,
        ),
    )

internal fun inspectorPropertyRow(
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
