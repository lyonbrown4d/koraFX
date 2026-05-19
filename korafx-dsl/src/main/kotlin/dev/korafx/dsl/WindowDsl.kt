package dev.korafx.dsl

import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.Node
import javafx.stage.Popup
import javafx.stage.Stage
import javafx.stage.Window

fun scene(
    root: Parent,
    width: Double = -1.0,
    height: Double = -1.0,
    init: Scene.() -> Unit = {},
): Scene =
    Scene(root, width, height).apply(init)

fun scene(
    width: Double = -1.0,
    height: Double = -1.0,
    root: () -> Parent,
): Scene =
    Scene(root(), width, height)

fun stage(
    title: String = "",
    owner: Window? = null,
    init: Stage.() -> Unit = {},
    content: StageBuilder.() -> Unit = {},
): Stage =
    Stage().apply {
        this.title = title
        if (owner != null) {
            initOwner(owner)
        }
        init()
        StageBuilder(this).content()
    }

class StageBuilder internal constructor(
    private val stage: Stage,
) {
    fun scene(
        width: Double = -1.0,
        height: Double = -1.0,
        root: () -> Parent,
    ): Scene =
        Scene(root(), width, height).also { scene ->
            stage.scene = scene
        }

    fun scene(
        root: Parent,
        width: Double = -1.0,
        height: Double = -1.0,
        init: Scene.() -> Unit = {},
    ): Scene =
        Scene(root, width, height).apply(init).also { scene ->
            stage.scene = scene
        }

    fun scene(scene: Scene): Scene =
        scene.also {
            stage.scene = it
        }

    fun onHidden(handler: () -> Unit) {
        stage.setOnHidden {
            handler()
        }
    }

    fun show() {
        stage.show()
    }
}

fun popup(
    autoFix: Boolean = true,
    autoHide: Boolean = false,
    hideOnEscape: Boolean = true,
    init: Popup.() -> Unit = {},
    content: PopupBuilder.() -> Unit = {},
): Popup =
    Popup().apply {
        isAutoFix = autoFix
        isAutoHide = autoHide
        isHideOnEscape = hideOnEscape
        init()
        PopupBuilder(this).apply(content)
    }

class PopupBuilder internal constructor(
    private val popup: Popup,
) {
    fun <T : Node> add(node: T): T {
        popup.content += node
        return node
    }

    fun content(vararg nodes: Node) {
        popup.content += nodes
    }
}
