package dev.korafx.devtools

import dev.korafx.framework.theme.KoraTheme
import dev.korafx.dsl.splitPane
import javafx.geometry.Bounds
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import java.util.Locale

internal fun TreeView<NodeDescriptor>.selectNode(node: Node) {
    val item = root?.findNode(node) ?: return
    item.expandParents()
    selectionModel.select(item)
    val row = getRow(item)
    if (row >= 0) {
        scrollTo(row)
    }
}

private fun TreeItem<NodeDescriptor>.findNode(node: Node): TreeItem<NodeDescriptor>? {
    if (value.node === node) {
        return this
    }
    children.forEach { child ->
        val match = child.findNode(node)
        if (match != null) {
            return match
        }
    }
    return null
}

private fun TreeItem<NodeDescriptor>.expandParents() {
    var cursor: TreeItem<NodeDescriptor>? = this
    while (cursor != null) {
        cursor.isExpanded = true
        cursor = cursor.parent
    }
}

internal fun describeNode(
    messages: DevtoolsMessages,
    node: Node?,
): String {
    if (node == null) {
        return messages.noNodeSelected
    }

    return buildString {
        appendLine("${messages.nodeType} = ${node.javaClass.name}")
        appendLine("${messages.nodeId} = ${node.id ?: ""}")
        appendLine("${messages.nodeStyleClass} = ${node.styleClass.joinToString()}")
        appendLine("${messages.visible} = ${node.isVisible}")
        appendLine("${messages.managed} = ${node.isManaged}")
        appendLine("${messages.disabled} = ${node.isDisabled}")
        appendLine("${messages.focused} = ${node.isFocused}")
        appendLine("${messages.hover} = ${node.isHover}")
        appendLine("boundsInLocal = ${node.boundsInLocal.format()}")
        appendLine("boundsInParent = ${node.boundsInParent.format()}")
        appendLine("layoutBounds = ${node.layoutBounds.format()}")
        appendLine("localToScene = ${node.localToScene(node.boundsInLocal).format()}")
        appendLine()
        appendLine(messages.pseudoClassStates)
        node.pseudoClassStates.sortedBy { it.pseudoClassName }.forEach { pseudoClass ->
            appendLine("- ${pseudoClass.pseudoClassName}")
        }
        appendLine()
        appendLine(messages.cssMetadata)
        node.cssMetaData.map { it.property }.sorted().forEach { property ->
            appendLine("- $property")
        }
    }
}

internal fun describeTheme(
    messages: DevtoolsMessages,
    theme: KoraTheme,
): String {
    val colors = theme.tokens.colors
    val typography = theme.tokens.typography
    val spacing = theme.tokens.spacing
    val radii = theme.tokens.radii

    return buildString {
        appendLine("id = ${theme.id}")
        appendLine("${messages.displayName} = ${theme.displayName}")
        appendLine()
        appendLine(messages.colors)
        appendLine("- primary = ${colors.primary}")
        appendLine("- success = ${colors.success}")
        appendLine("- warning = ${colors.warning}")
        appendLine("- danger = ${colors.danger}")
        appendLine("- info = ${colors.info}")
        appendLine("- surface = ${colors.surface}")
        appendLine("- surfaceMuted = ${colors.surfaceMuted}")
        appendLine("- textPrimary = ${colors.textPrimary}")
        appendLine("- textSecondary = ${colors.textSecondary}")
        appendLine("- border = ${colors.border}")
        appendLine()
        appendLine(messages.typography)
        appendLine("- fontFamily = ${typography.fontFamily}")
        appendLine("- baseSize = ${typography.baseSize}")
        appendLine("- headlineSize = ${typography.headlineSize}")
        appendLine()
        appendLine(messages.spacing)
        appendLine("- xs = ${spacing.xs}")
        appendLine("- sm = ${spacing.sm}")
        appendLine("- md = ${spacing.md}")
        appendLine("- lg = ${spacing.lg}")
        appendLine("- xl = ${spacing.xl}")
        appendLine()
        appendLine(messages.radii)
        appendLine("- small = ${radii.small}")
        appendLine("- medium = ${radii.medium}")
        appendLine("- large = ${radii.large}")
        appendLine("- pill = ${radii.pill}")
    }
}

private fun Bounds.format(): String =
    "x=${minX.formatDouble()}, y=${minY.formatDouble()}, " +
        "w=${width.formatDouble()}, h=${height.formatDouble()}"

private fun Double.formatDouble(): String =
    String.format(Locale.US, "%.1f", this)

internal class SplitPaneBuilder(
    private val first: Node,
    private val second: Node,
) {
    private var orientation: Orientation = Orientation.HORIZONTAL
    private var divider: Double = 0.5

    fun orientation(value: Orientation): SplitPaneBuilder =
        apply {
            orientation = value
        }

    fun divider(value: Double): SplitPaneBuilder =
        apply {
            divider = value
        }

    fun build(): javafx.scene.control.SplitPane =
        splitPane(orientation = orientation) {
            add(first)
            add(second)
        }.apply {
            setDividerPositions(divider)
        }
}
