package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.styleClass
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane

fun borderLayout(
    init: BorderPane.() -> Unit = {},
    content: BorderLayoutBuilder.() -> Unit,
): BorderPane =
    BorderPane().apply {
        styleClass("border-layout")
        init()
        BorderLayoutBuilder(this).content()
    }

class BorderLayoutBuilder internal constructor(
    private val pane: BorderPane,
) {
    fun top(node: Node) {
        pane.top = node.withSlot("border-layout-top")
    }

    fun top(factory: () -> Node) {
        top(factory())
    }

    fun header(node: Node) {
        top(node)
    }

    fun header(factory: () -> Node) {
        top(factory)
    }

    fun right(node: Node) {
        pane.right = node.withSlot("border-layout-right")
    }

    fun right(factory: () -> Node) {
        right(factory())
    }

    fun bottom(node: Node) {
        pane.bottom = node.withSlot("border-layout-bottom")
    }

    fun bottom(factory: () -> Node) {
        bottom(factory())
    }

    fun footer(node: Node) {
        bottom(node)
    }

    fun footer(factory: () -> Node) {
        bottom(factory)
    }

    fun left(node: Node) {
        pane.left = node.withSlot("border-layout-left")
    }

    fun left(factory: () -> Node) {
        left(factory())
    }

    fun sidebar(node: Node) {
        left(node)
    }

    fun sidebar(factory: () -> Node) {
        left(factory)
    }

    fun center(node: Node) {
        pane.center = node.withSlot("border-layout-center")
    }

    fun center(factory: () -> Node) {
        center(factory())
    }

    fun content(node: Node) {
        center(node)
    }

    fun content(factory: () -> Node) {
        center(factory)
    }

    private fun Node.withSlot(styleClass: String): Node =
        apply {
            styleClass(styleClass)
        }
}

fun NodeContainerBuilder.borderLayout(
    init: BorderPane.() -> Unit = {},
    content: BorderLayoutBuilder.() -> Unit,
): BorderPane =
    add(
        dev.korafx.components.borderLayout(
            init = init,
            content = content,
        ),
    )

class WorkspaceLayout internal constructor() : StackPane() {
    val frame: BorderPane = BorderPane()
    val body: BorderPane = BorderPane()
    val overlayLayer: StackPane = StackPane()

    var navigationNode: Node? = null
        private set
    var detailsNode: Node? = null
        private set

    init {
        styleClass("workspace-layout")
        frame.styleClass("workspace-layout-frame")
        body.styleClass("workspace-layout-body")
        overlayLayer.styleClass("workspace-layout-overlay")
        overlayLayer.isPickOnBounds = false
        frame.center = body
        children += frame
        children += overlayLayer
    }

    fun setTopBar(node: Node?) {
        frame.top = node?.withWorkspaceSlot("workspace-layout-top-bar")
    }

    fun setNavigation(node: Node?) {
        navigationNode = node?.withWorkspaceSlot("workspace-layout-navigation")
        body.left = navigationNode
    }

    fun setContent(node: Node?) {
        body.center = node?.withWorkspaceSlot("workspace-layout-content")
    }

    fun setDetails(node: Node?) {
        detailsNode = node?.withWorkspaceSlot("workspace-layout-details")
        body.right = detailsNode
    }

    fun setStatus(node: Node?) {
        frame.bottom = node?.withWorkspaceSlot("workspace-layout-status")
    }

    fun setNavigationVisible(
        visible: Boolean,
        manageWhenHidden: Boolean = true,
    ) {
        navigationNode?.setSlotVisible(visible, manageWhenHidden)
    }

    fun setDetailsVisible(
        visible: Boolean,
        manageWhenHidden: Boolean = true,
    ) {
        detailsNode?.setSlotVisible(visible, manageWhenHidden)
    }

    fun addOverlay(
        node: Node,
        alignment: Pos = Pos.BOTTOM_RIGHT,
        margin: Insets = Insets(16.0),
    ) {
        node.styleClass("workspace-layout-overlay-item")
        StackPane.setAlignment(node, alignment)
        StackPane.setMargin(node, margin)
        overlayLayer.children += node
    }

    private fun Node.setSlotVisible(
        visible: Boolean,
        manageWhenHidden: Boolean,
    ) {
        isVisible = visible
        if (manageWhenHidden) {
            isManaged = visible
        }
    }
}

fun workspaceLayout(
    init: WorkspaceLayout.() -> Unit = {},
    content: WorkspaceLayoutBuilder.() -> Unit,
): WorkspaceLayout =
    WorkspaceLayout().apply {
        init()
        WorkspaceLayoutBuilder(this).content()
    }

class WorkspaceLayoutBuilder internal constructor(
    private val workspace: WorkspaceLayout,
) {
    fun topBar(node: Node) {
        workspace.setTopBar(node)
    }

    fun topBar(factory: () -> Node) {
        topBar(factory())
    }

    fun toolbar(node: Node) {
        topBar(node)
    }

    fun toolbar(factory: () -> Node) {
        topBar(factory)
    }

    fun navigation(node: Node) {
        workspace.setNavigation(node)
    }

    fun navigation(factory: () -> Node) {
        navigation(factory())
    }

    fun sidebar(node: Node) {
        navigation(node)
    }

    fun sidebar(factory: () -> Node) {
        navigation(factory)
    }

    fun content(node: Node) {
        workspace.setContent(node)
    }

    fun content(factory: () -> Node) {
        content(factory())
    }

    fun details(node: Node) {
        workspace.setDetails(node)
    }

    fun details(factory: () -> Node) {
        details(factory())
    }

    fun inspector(node: Node) {
        details(node)
    }

    fun inspector(factory: () -> Node) {
        details(factory)
    }

    fun status(node: Node) {
        workspace.setStatus(node)
    }

    fun status(factory: () -> Node) {
        status(factory())
    }

    fun footer(node: Node) {
        status(node)
    }

    fun footer(factory: () -> Node) {
        status(factory)
    }

    fun overlay(
        node: Node,
        alignment: Pos = Pos.BOTTOM_RIGHT,
        margin: Insets = Insets(16.0),
    ) {
        workspace.addOverlay(node, alignment, margin)
    }

    fun overlay(
        alignment: Pos = Pos.BOTTOM_RIGHT,
        margin: Insets = Insets(16.0),
        factory: () -> Node,
    ) {
        overlay(factory(), alignment, margin)
    }

    fun navigationVisible(
        visible: Boolean,
        manageWhenHidden: Boolean = true,
    ) {
        workspace.setNavigationVisible(visible, manageWhenHidden)
    }

    fun detailsVisible(
        visible: Boolean,
        manageWhenHidden: Boolean = true,
    ) {
        workspace.setDetailsVisible(visible, manageWhenHidden)
    }
}

fun NodeContainerBuilder.workspaceLayout(
    init: WorkspaceLayout.() -> Unit = {},
    content: WorkspaceLayoutBuilder.() -> Unit,
): WorkspaceLayout =
    add(
        dev.korafx.components.workspaceLayout(
            init = init,
            content = content,
        ),
    )

private fun Node.withWorkspaceSlot(styleClass: String): Node =
    apply {
        styleClass(styleClass)
    }
