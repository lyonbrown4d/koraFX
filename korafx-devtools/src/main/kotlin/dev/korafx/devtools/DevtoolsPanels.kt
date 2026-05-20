package dev.korafx.devtools

import dev.korafx.components.setKoraIcon
import dev.korafx.dsl.HBoxBuilder
import dev.korafx.dsl.borderPane
import dev.korafx.dsl.comboBox
import dev.korafx.dsl.hbox
import dev.korafx.dsl.label
import dev.korafx.dsl.listView
import dev.korafx.dsl.onAction
import dev.korafx.dsl.paddingAll
import dev.korafx.dsl.textArea
import dev.korafx.dsl.textField
import dev.korafx.dsl.treeItem
import dev.korafx.dsl.treeView
import dev.korafx.framework.KoraApplication
import dev.korafx.navigation.PathRoute
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.TreeItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons

internal class DevtoolsPanels(
    private val app: KoraApplication,
    private val inspectedRoot: () -> Parent,
    private val messages: DevtoolsMessages,
    private val selection: DevtoolsSelectionModel,
    private val actions: DevtoolsActions,
    private val jobSink: (Job) -> Unit,
) {
    fun render(panel: KoraDevtoolsPanel): Node =
        when (panel) {
            KoraDevtoolsPanel.SceneGraph -> createSceneGraphPanel()
            KoraDevtoolsPanel.Inspector -> createInspectorPanel()
            KoraDevtoolsPanel.Navigation -> createNavigationPanel()
            KoraDevtoolsPanel.Theme -> createThemePanel()
        }

    private fun createSceneGraphPanel(): Node {
        val tree = treeView<NodeDescriptor>()
        val details = textArea {
            isEditable = false
            isWrapText = false
        }

        fun refreshTree() {
            tree.root = createTreeItem(inspectedRoot())
            tree.root.isExpanded = true
        }

        tree.selectionModel.selectedItemProperty().addListener { _, _, item ->
            val node = item?.value?.node
            selection.select(node)
            details.text = describeNode(messages, node)
        }
        selection.addWindowListener(
            ChangeListener { _, _, node ->
                if (node != null && tree.selectionModel.selectedItem?.value?.node !== node) {
                    tree.selectNode(node)
                }
                details.text = describeNode(messages, node)
            },
        )
        refreshTree()

        val left = borderPane {
            top {
                toolbar(messages.liveTree) {
                    button(messages.pickNode) {
                        setKoraIcon(BootstrapIcons.CURSOR)
                        onAction {
                            actions.startPicking()
                        }
                    }
                    button(messages.clear) {
                        setKoraIcon(BootstrapIcons.X_CIRCLE)
                        onAction {
                            actions.clearSelection()
                            tree.selectionModel.clearSelection()
                        }
                    }
                    button(messages.refresh) {
                        setKoraIcon(BootstrapIcons.ARROW_CLOCKWISE)
                        onAction {
                            refreshTree()
                            selection.selectedNode.get()?.let(tree::selectNode)
                        }
                    }
                }
            }
            center(tree)
        }

        return SplitPaneBuilder(left, details)
            .orientation(Orientation.HORIZONTAL)
            .divider(0.42)
            .build()
    }

    private fun createInspectorPanel(): Node {
        val details = textArea {
            isEditable = false
            isWrapText = false
            text = messages.selectNodeHint
        }
        selection.addWindowListener(
            ChangeListener { _, _, node ->
                details.text = describeNode(messages, node)
            },
        )

        return borderPane {
            top {
                toolbar(messages.selectedNode) {
                    button(messages.pickNode) {
                        setKoraIcon(BootstrapIcons.CURSOR)
                        onAction {
                            actions.startPicking()
                        }
                    }
                    button(messages.clear) {
                        setKoraIcon(BootstrapIcons.X_CIRCLE)
                        onAction {
                            actions.clearSelection()
                        }
                    }
                    button(messages.refresh) {
                        setKoraIcon(BootstrapIcons.ARROW_CLOCKWISE)
                        onAction {
                            details.text = describeNode(messages, selection.selectedNode.get())
                        }
                    }
                }
            }
            center(details)
        }
    }

    private fun createNavigationPanel(): Node {
        val routeList = listView<RouteRow> {
            render { item ->
                if (item.active) {
                    "-> ${item.route.title} (${item.route.id}) ${item.path}"
                } else {
                    "${item.route.title} (${item.route.id}) ${item.path}"
                }
            }
        }
        val pathField = textField {
            promptText = "/path?query=value#hash"
            prefColumnCount = 24
        }
        val stateArea = textArea {
            isEditable = false
            prefRowCount = 10
        }

        fun refresh() {
            val state = app.navigator.state.value
            routeList.items = FXCollections.observableArrayList(
                state.routes.map { route ->
                    RouteRow(
                        route = route,
                        path = route.pathLabel(),
                        active = route.id == state.currentRoute.id,
                    )
                },
            )
            if (!pathField.isFocused) {
                pathField.text = state.currentLocation.fullPath
            }
            stateArea.text = buildString {
                appendLine("${messages.currentRoute} = ${state.currentRoute.title} (${state.currentRoute.id})")
                appendLine("${messages.currentLocation} = ${state.currentLocation.fullPath}")
                appendLine("${messages.routePath} = ${state.currentLocation.path}")
                if (state.currentLocation.params.isNotEmpty()) {
                    appendLine("params = ${state.currentLocation.params}")
                }
                if (state.currentLocation.query.values.isNotEmpty()) {
                    appendLine("query = ${state.currentLocation.query.values}")
                }
                state.currentLocation.hash?.let { hash ->
                    appendLine("hash = $hash")
                }
                appendLine("${messages.pageInstancePolicy} = ${state.pageInstancePolicy}")
                appendLine("${messages.routes} = ${state.routes.size}")
                appendLine("${messages.backStack} = ${state.backStack.map { it.fullPath }}")
                appendLine("${messages.forwardStack} = ${state.forwardStack.map { it.fullPath }}")
            }
        }

        jobSink(
            app.uiScope.launch {
                app.navigator.state.collectLatest {
                    refresh()
                }
            },
        )
        refresh()

        return borderPane {
            top {
                toolbar(messages.registeredRoutes) {
                    add(pathField)
                    button(messages.navigatePath) {
                        setKoraIcon(BootstrapIcons.SIGNPOST_SPLIT)
                        onAction {
                            app.uiScope.launch {
                                app.navigator.navigatePathAsync(pathField.text)
                            }
                        }
                    }
                    button(messages.navigate) {
                        setKoraIcon(BootstrapIcons.ARROW_RIGHT_CIRCLE)
                        onAction {
                            routeList.selectionModel.selectedItem?.route?.let { route ->
                                app.uiScope.launch {
                                    app.navigator.navigateAsync(route.id)
                                }
                            }
                        }
                    }
                    button(messages.back) {
                        setKoraIcon(BootstrapIcons.ARROW_LEFT_CIRCLE)
                        onAction {
                            app.uiScope.launch {
                                app.navigator.backAsync()
                            }
                        }
                    }
                    button(messages.forward) {
                        setKoraIcon(BootstrapIcons.ARROW_RIGHT_CIRCLE)
                        onAction {
                            app.uiScope.launch {
                                app.navigator.forwardAsync()
                            }
                        }
                    }
                    button(messages.refresh) {
                        setKoraIcon(BootstrapIcons.ARROW_CLOCKWISE)
                        onAction {
                            refresh()
                        }
                    }
                }
            }
            center(routeList)
            bottom(stateArea)
        }
    }

    private fun dev.korafx.navigation.Route.pathLabel(): String =
        (this as? PathRoute)?.path ?: id

    private fun createThemePanel(): Node {
        var updating = false
        val combo = comboBox(app.themeManager.availableThemes) {
            render { theme ->
                "${theme.displayName} (${theme.id})"
            }
        }
        val details = textArea {
            isEditable = false
            isWrapText = false
        }

        combo.selectionModel.selectedItemProperty().addListener { _, _, theme ->
            if (theme != null && !updating) {
                app.themeManager.setTheme(theme)
                details.text = describeTheme(messages, theme)
            }
        }

        jobSink(
            app.uiScope.launch {
                app.themeManager.theme.collectLatest { theme ->
                    updating = true
                    combo.selectionModel.select(theme)
                    details.text = describeTheme(messages, theme)
                    updating = false
                }
            },
        )

        combo.selectionModel.select(app.themeManager.currentTheme())
        details.text = describeTheme(messages, app.themeManager.currentTheme())

        return borderPane {
            top {
                toolbar(messages.activeTheme) {
                    add(combo)
                }
            }
            center(details)
        }
    }

    private fun createTreeItem(node: Node): TreeItem<NodeDescriptor> =
        treeItem(NodeDescriptor(node)).apply {
            if (node is Parent) {
                children.setAll(node.childrenUnmodifiable.map(::createTreeItem))
            }
        }

    private fun toolbar(
        title: String,
        actions: HBoxBuilder.() -> Unit = {},
    ): Node =
        hbox(
            spacing = 8.0,
            init = {
                paddingAll(10.0)
            },
        ) {
            label(title)
            spacer()
            actions()
        }
}
