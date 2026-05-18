package dev.korafx.sample

import dev.korafx.sample.di.WorkbenchAppGraph
import dev.korafx.sample.ui.WorkbenchRootView
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

class SampleWorkbenchApp : Application() {
    private lateinit var graph: WorkbenchAppGraph

    override fun start(stage: Stage) {
        graph = WorkbenchAppGraph.create()
        val scene = Scene(WorkbenchRootView(graph).buildRoot(), 1120.0, 720.0)
        graph.themeController.bind(scene)

        stage.title = "KoraFX Workbench"
        stage.scene = scene
        stage.show()
    }

    override fun stop() {
        if (::graph.isInitialized) {
            graph.close()
        }
    }
}
