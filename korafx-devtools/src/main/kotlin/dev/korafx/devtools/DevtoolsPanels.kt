package dev.korafx.devtools

import dev.korafx.components.setKoraIcon
import dev.korafx.dsl.borderPane
import dev.korafx.dsl.onAction
import dev.korafx.dsl.textArea
import dev.korafx.dsl.treeItem
import dev.korafx.dsl.treeView
import dev.korafx.framework.KoraApplication
import javafx.beans.value.ChangeListener
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.TreeItem
import kotlinx.coroutines.Job
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
            KoraDevtoolsPanel.Navigation -> createDevtoolsNavigationPanel(app, messages, jobSink)
            KoraDevtoolsPanel.Theme -> createDevtoolsThemePanel(app, messages, jobSink)
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
                devtoolsToolbar(messages.liveTree) {
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
                devtoolsToolbar(messages.selectedNode) {
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

    private fun createTreeItem(node: Node): TreeItem<NodeDescriptor> =
        treeItem(NodeDescriptor(node)).apply {
            if (node is Parent) {
                children.setAll(node.childrenUnmodifiable.map(::createTreeItem))
            }
        }
}
