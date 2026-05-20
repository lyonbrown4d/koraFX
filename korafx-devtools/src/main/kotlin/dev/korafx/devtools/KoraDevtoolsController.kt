package dev.korafx.devtools

import dev.korafx.dsl.rectangle
import dev.korafx.dsl.scene
import dev.korafx.dsl.splitPane
import dev.korafx.dsl.stackPane
import dev.korafx.dsl.stage
import dev.korafx.dsl.styleClass
import dev.korafx.framework.KoraApplication
import dev.korafx.framework.theme.SceneThemeController
import dev.korafx.framework.theme.ThemeStyleClass
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.scene.control.SplitPane
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import kotlinx.coroutines.Job

internal interface DevtoolsActions {
    fun startPicking()

    fun clearSelection()

    fun setPlacement(placement: KoraDevtoolsPlacement)
}

internal class KoraDevtoolsController(
    private val app: KoraApplication,
    private val spec: KoraDevtoolsSpec,
    private val messages: DevtoolsMessages,
    private val selection: DevtoolsSelectionModel,
    private val highlighter: NodeHighlighter,
) : DevtoolsActions {
    private var stage: Stage? = null
    private var currentPlacement: KoraDevtoolsPlacement = spec.placement
    private var dockRoot: SplitPane? = null
    private var inspectedHost: StackPane? = null
    private var devtoolsRoot: Parent? = null
    private var originalRoot: Parent? = null
    private var themeController: SceneThemeController? = null
    private val jobs = mutableListOf<Job>()
    private val inspector = InProcessInspector(
        scene = app.scene,
        selection = selection,
        highlighter = highlighter,
        inspectedRoot = ::inspectedRoot,
        excludedRoots = { listOfNotNull(devtoolsRoot) },
        highlightSelection = spec.highlightSelection,
    )
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

    fun install() {
        app.scene.addEventFilter(KeyEvent.KEY_PRESSED, shortcutHandler)
        selection.selectedNode.addListener { _, _, node ->
            inspector.highlight(node)
        }
    }

    fun dispose() {
        app.scene.removeEventFilter(KeyEvent.KEY_PRESSED, shortcutHandler)
        inspector.stopPicking()
        close()
    }

    override fun startPicking() {
        open()
        inspector.startPicking()
    }

    override fun clearSelection() {
        selection.clear()
        inspector.hideHighlight()
    }

    override fun setPlacement(placement: KoraDevtoolsPlacement) {
        Platform.runLater {
            if (placement == currentPlacement && isOpen()) {
                stage?.requestFocus()
                return@runLater
            }

            if (isOpen()) {
                close()
            }
            currentPlacement = placement
            open()
        }
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
        when (currentPlacement) {
            KoraDevtoolsPlacement.LEFT,
            KoraDevtoolsPlacement.RIGHT,
            KoraDevtoolsPlacement.BOTTOM,
            -> openDocked()
            KoraDevtoolsPlacement.WINDOW -> openWindow()
        }
    }

    private fun close() {
        inspector.stopPicking()
        when (currentPlacement) {
            KoraDevtoolsPlacement.LEFT,
            KoraDevtoolsPlacement.RIGHT,
            KoraDevtoolsPlacement.BOTTOM,
            -> closeDocked()
            KoraDevtoolsPlacement.WINDOW -> closeWindow()
        }
    }

    private fun isOpen(): Boolean =
        when (currentPlacement) {
            KoraDevtoolsPlacement.LEFT,
            KoraDevtoolsPlacement.RIGHT,
            KoraDevtoolsPlacement.BOTTOM,
            -> dockRoot != null
            KoraDevtoolsPlacement.WINDOW -> stage?.isShowing == true
        }

    private fun openDocked() {
        if (dockRoot != null) {
            return
        }

        val currentRoot = app.scene.root
        val host = createInspectedHost()
        val shell = createShell { currentRoot }.apply {
            styleClass("korafx-devtools-docked")
            if (this is Region) {
                minHeight = 240.0
                prefHeight = spec.dockHeight
                minWidth = 280.0
                prefWidth = spec.dockWidth
            }
        }
        val container = splitPane(
            orientation = currentPlacement.dockOrientation(),
            init = {
                styleClass(ThemeStyleClass.Root)
                styleClass("korafx-devtools-dock-host")
            },
        ) {
            when (currentPlacement) {
                KoraDevtoolsPlacement.LEFT -> {
                    add(shell)
                    add(host)
                }
                KoraDevtoolsPlacement.RIGHT,
                KoraDevtoolsPlacement.BOTTOM,
                -> {
                    add(host)
                    add(shell)
                }
                KoraDevtoolsPlacement.WINDOW -> Unit
            }
        }

        originalRoot = currentRoot
        inspectedHost = host
        devtoolsRoot = shell
        highlighter.limitTo(host)
        dockRoot = container
        app.scene.root = container
        host.children.setAll(currentRoot)
        highlighter.renderIn(host)
        container.setDividerPositions(currentPlacement.initialDivider(app.scene.width, app.scene.height))
    }

    private fun closeDocked() {
        val container = dockRoot ?: return
        val root = originalRoot

        disposeRenderedResources()
        inspectedHost?.children?.clear()
        highlighter.limitTo(null)
        highlighter.renderIn(null)
        container.items.clear()
        if (root != null && app.scene.root === container) {
            app.scene.root = root
        }
        dockRoot = null
        inspectedHost = null
        devtoolsRoot = null
        originalRoot = null
    }

    private fun openWindow() {
        if (stage?.isShowing == true) {
            stage?.requestFocus()
            return
        }

        installWindowOverlay()
        val shell = createShell()
        devtoolsRoot = shell
        val scene = scene(shell, spec.width, spec.height)
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
                restoreWindowOverlay()
            }
            show()
        }
    }

    private fun closeWindow() {
        val window = stage
        if (window != null) {
            window.close()
        } else {
            disposeRenderedResources()
            restoreWindowOverlay()
        }
    }

    private fun disposeRenderedResources() {
        inspector.stopPicking()
        inspector.hideHighlight()
        jobs.forEach(Job::cancel)
        jobs.clear()
        selection.disposeWindowListeners()
        highlighter.limitTo(null)
        highlighter.renderIn(null)
        themeController?.dispose()
        themeController = null
        stage = null
    }

    private fun installWindowOverlay() {
        val currentRoot = originalRoot ?: app.scene.root
        if (inspectedHost != null && app.scene.root === inspectedHost) {
            highlighter.limitTo(inspectedHost)
            highlighter.renderIn(inspectedHost)
            return
        }

        val host = createInspectedHost()
        originalRoot = currentRoot
        inspectedHost = host
        app.scene.root = host
        host.children.setAll(currentRoot)
        highlighter.limitTo(host)
        highlighter.renderIn(host)
    }

    private fun restoreWindowOverlay() {
        val host = inspectedHost
        val root = originalRoot
        highlighter.limitTo(null)
        highlighter.renderIn(null)
        host?.children?.clear()
        if (host != null && root != null && app.scene.root === host) {
            app.scene.root = root
        }
        inspectedHost = null
        devtoolsRoot = null
        originalRoot = null
    }

    private fun createInspectedHost(): StackPane =
        stackPane(
            init = {
                styleClass("korafx-devtools-inspected-host")
                isMouseTransparent = true
                isPickOnBounds = false
                val host = this
                clip = rectangle {
                    widthProperty().bind(host.widthProperty())
                    heightProperty().bind(host.heightProperty())
                }
            },
        ) {}

    private fun createShell(inspectedRoot: () -> Parent = ::inspectedRoot): Parent =
        DevtoolsShell(
            app = app,
            spec = spec,
            messages = messages,
            selection = selection,
            actions = this,
            jobSink = ::track,
            inspectedRoot = inspectedRoot,
        ).build()

    private fun inspectedRoot(): Parent =
        originalRoot ?: app.scene.root

    private fun KoraDevtoolsPlacement.dockOrientation(): Orientation =
        when (this) {
            KoraDevtoolsPlacement.BOTTOM -> Orientation.VERTICAL
            KoraDevtoolsPlacement.LEFT,
            KoraDevtoolsPlacement.RIGHT,
            KoraDevtoolsPlacement.WINDOW,
            -> Orientation.HORIZONTAL
        }

    private fun KoraDevtoolsPlacement.initialDivider(
        sceneWidth: Double,
        sceneHeight: Double,
    ): Double =
        when (this) {
            KoraDevtoolsPlacement.LEFT ->
                (spec.dockWidth / sceneWidth.validSize()).coerceIn(0.15, 0.85)
            KoraDevtoolsPlacement.RIGHT ->
                ((sceneWidth.validSize() - spec.dockWidth) / sceneWidth.validSize()).coerceIn(0.15, 0.85)
            KoraDevtoolsPlacement.BOTTOM ->
                ((sceneHeight.validSize() - spec.dockHeight) / sceneHeight.validSize()).coerceIn(0.15, 0.85)
            KoraDevtoolsPlacement.WINDOW -> 0.5
        }

    private fun Double.validSize(): Double =
        takeIf { it.isFinite() && it > 0.0 } ?: 1.0
}
