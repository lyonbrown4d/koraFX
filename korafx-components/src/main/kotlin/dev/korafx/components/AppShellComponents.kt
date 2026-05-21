package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.styleClass
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane

class AppShell internal constructor() : StackPane() {
    val frame: BorderPane = BorderPane()
    val body: BorderPane = BorderPane()
    val overlayLayer: StackPane = StackPane()

    var navigationNode: Node? = null
        private set
    var detailsNode: Node? = null
        private set

    init {
        styleClass("app-shell")
        frame.styleClass("app-shell-frame")
        body.styleClass("app-shell-body")
        overlayLayer.styleClass("app-shell-overlay")
        overlayLayer.isPickOnBounds = false
        frame.center = body
        children += frame
        children += overlayLayer
    }

    fun setTopBar(node: Node?) {
        frame.top = node?.withAppShellSlot("app-shell-top-bar")
    }

    fun setNavigation(node: Node?) {
        navigationNode = node?.withAppShellSlot("app-shell-navigation")
        body.left = navigationNode
    }

    fun setContent(node: Node?) {
        body.center = node?.withAppShellSlot("app-shell-content")
    }

    fun setDetails(node: Node?) {
        detailsNode = node?.withAppShellSlot("app-shell-details")
        body.right = detailsNode
    }

    fun setFooter(node: Node?) {
        frame.bottom = node?.withAppShellSlot("app-shell-footer")
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
        node.styleClass("app-shell-overlay-item")
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

fun appShell(
    init: AppShell.() -> Unit = {},
    content: AppShellBuilder.() -> Unit,
): AppShell =
    AppShell().apply {
        init()
        AppShellBuilder(this).content()
    }

class AppShellBuilder internal constructor(
    private val shell: AppShell,
) {
    fun topBar(node: Node) {
        shell.setTopBar(node)
    }

    fun topBar(factory: () -> Node) {
        topBar(factory())
    }

    fun header(node: Node) {
        topBar(node)
    }

    fun header(factory: () -> Node) {
        topBar(factory)
    }

    fun navigation(node: Node) {
        shell.setNavigation(node)
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
        shell.setContent(node)
    }

    fun content(factory: () -> Node) {
        content(factory())
    }

    fun details(node: Node) {
        shell.setDetails(node)
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

    fun footer(node: Node) {
        shell.setFooter(node)
    }

    fun footer(factory: () -> Node) {
        footer(factory())
    }

    fun status(node: Node) {
        footer(node)
    }

    fun status(factory: () -> Node) {
        footer(factory)
    }

    fun overlay(
        node: Node,
        alignment: Pos = Pos.BOTTOM_RIGHT,
        margin: Insets = Insets(16.0),
    ) {
        shell.addOverlay(node, alignment, margin)
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
        shell.setNavigationVisible(visible, manageWhenHidden)
    }

    fun detailsVisible(
        visible: Boolean,
        manageWhenHidden: Boolean = true,
    ) {
        shell.setDetailsVisible(visible, manageWhenHidden)
    }
}

fun NodeContainerBuilder.appShell(
    init: AppShell.() -> Unit = {},
    content: AppShellBuilder.() -> Unit,
): AppShell =
    add(
        dev.korafx.components.appShell(
            init = init,
            content = content,
        ),
    )

private fun Node.withAppShellSlot(styleClass: String): Node =
    apply {
        styleClass(styleClass)
    }
