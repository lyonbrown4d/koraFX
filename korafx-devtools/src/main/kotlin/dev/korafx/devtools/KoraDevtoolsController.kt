package dev.korafx.devtools

import dev.korafx.framework.KoraApplication
import dev.korafx.framework.theme.SceneThemeController
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.dsl.borderPane
import dev.korafx.dsl.scene
import dev.korafx.dsl.stage
import dev.korafx.dsl.styleClass
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import javafx.stage.Stage
import kotlinx.coroutines.Job

internal interface DevtoolsActions {
    fun startPicking()

    fun clearSelection()
}

internal class KoraDevtoolsController(
    private val app: KoraApplication,
    private val spec: KoraDevtoolsSpec,
    private val messages: DevtoolsMessages,
    private val selection: DevtoolsSelectionModel,
    private val highlighter: NodeHighlighter,
) : DevtoolsActions {
    private var stage: Stage? = null
    private var dockRoot: BorderPane? = null
    private var originalRoot: Parent? = null
    private var themeController: SceneThemeController? = null
    private val jobs = mutableListOf<Job>()
    private var picking = false
    private val shortcutHandler = EventHandler<KeyEvent> { event ->
        if (spec.shortcut.match(event)) {
            event.consume()
            toggle()
        } else if (spec.pickerShortcut.match(event)) {
            event.consume()
            open()
            startPicking()
        }
    }
    private val pickMoveHandler = EventHandler<MouseEvent> { event ->
        if (picking) {
            highlight(event.pickResult.intersectedNode)
        }
    }
    private val pickClickHandler = EventHandler<MouseEvent> { event ->
        if (picking) {
            event.consume()
            selection.select(event.pickResult.intersectedNode)
            stopPicking()
            open()
        }
    }

    fun install() {
        app.scene.addEventFilter(KeyEvent.KEY_PRESSED, shortcutHandler)
        selection.selectedNode.addListener { _, _, node ->
            highlight(node)
        }
    }

    fun dispose() {
        app.scene.removeEventFilter(KeyEvent.KEY_PRESSED, shortcutHandler)
        stopPicking()
        close()
    }

    override fun startPicking() {
        if (picking) {
            return
        }
        picking = true
        app.scene.addEventFilter(MouseEvent.MOUSE_MOVED, pickMoveHandler)
        app.scene.addEventFilter(MouseEvent.MOUSE_CLICKED, pickClickHandler)
    }

    override fun clearSelection() {
        selection.clear()
        highlighter.hide()
    }

    fun track(job: Job) {
        jobs += job
    }

    private fun toggle() {
        if (isOpen()) {
            close()
        } else {
            open()
        }
    }

    private fun open() {
        when (spec.placement) {
            KoraDevtoolsPlacement.BOTTOM -> openDocked()
            KoraDevtoolsPlacement.WINDOW -> openWindow()
        }
    }

    private fun close() {
        when (spec.placement) {
            KoraDevtoolsPlacement.BOTTOM -> closeDocked()
            KoraDevtoolsPlacement.WINDOW -> closeWindow()
        }
    }

    private fun isOpen(): Boolean =
        when (spec.placement) {
            KoraDevtoolsPlacement.BOTTOM -> dockRoot != null
            KoraDevtoolsPlacement.WINDOW -> stage?.isShowing == true
        }

    private fun openDocked() {
        if (dockRoot != null) {
            return
        }

        val currentRoot = app.scene.root
        val shell = createShell { currentRoot }.apply {
            styleClass("korafx-devtools-docked")
            if (this is Region) {
                minHeight = 240.0
                prefHeight = spec.dockHeight
            }
        }
        val container = borderPane(
            init = {
                styleClass(ThemeStyleClass.Root)
                styleClass("korafx-devtools-dock-host")
            },
        ) {
            center(currentRoot)
            bottom(shell)
        }

        originalRoot = currentRoot
        dockRoot = container
        app.scene.root = container
    }

    private fun closeDocked() {
        val container = dockRoot ?: return
        val root = originalRoot

        disposeRenderedResources()
        container.bottom = null
        container.center = null
        if (root != null && app.scene.root === container) {
            app.scene.root = root
        }
        dockRoot = null
        originalRoot = null
    }

    private fun openWindow() {
        if (stage?.isShowing == true) {
            stage?.requestFocus()
            return
        }

        val scene = scene(createShell(), spec.width, spec.height)
        val controller = SceneThemeController(app.themeManager)
        controller.bind(scene)
        themeController = controller

        stage = stage(
            title = spec.title,
            owner = app.stage,
        ) {
            scene(scene)
            onHidden {
                disposeRenderedResources()
            }
            show()
        }
    }

    private fun closeWindow() {
        stage?.close()
        disposeRenderedResources()
    }

    private fun disposeRenderedResources() {
        highlighter.hide()
        jobs.forEach(Job::cancel)
        jobs.clear()
        selection.disposeWindowListeners()
        themeController?.dispose()
        themeController = null
        stage = null
    }

    private fun createShell(inspectedRoot: () -> Parent = { app.scene.root }): Parent =
        DevtoolsShell(
            app = app,
            spec = spec,
            messages = messages,
            selection = selection,
            actions = this,
            jobSink = ::track,
            inspectedRoot = inspectedRoot,
        ).build()

    private fun stopPicking() {
        if (!picking) {
            return
        }
        picking = false
        app.scene.removeEventFilter(MouseEvent.MOUSE_MOVED, pickMoveHandler)
        app.scene.removeEventFilter(MouseEvent.MOUSE_CLICKED, pickClickHandler)
    }

    private fun highlight(node: Node?) {
        if (spec.highlightSelection) {
            highlighter.show(node)
        }
    }
}
