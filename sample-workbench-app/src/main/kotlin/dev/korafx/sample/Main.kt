package dev.korafx.sample

import dev.korafx.framework.koraApplication
import dev.korafx.framework.theme.BuiltInThemes
import dev.korafx.sample.di.WorkbenchAppGraph
import dev.korafx.sample.di.workbenchModule
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.ui.WorkbenchRootView
import dev.korafx.sample.viewmodel.WorkbenchViewModel

fun main(args: Array<String>) = koraApplication(args) {
  window {
    title = "KoraFX Workbench"
    width = 1120.0
    height = 720.0
  }

  installKoin {
    modules(workbenchModule())
  }

  theme {
    presets(BuiltInThemes.all)
    default(BuiltInThemes.Nord)
    persistSelection = true
  }

  navigation {
    initialRoute = WorkbenchRoute.Overview
    routes(WorkbenchRoute.all)
  }

  content {
    WorkbenchRootView(WorkbenchAppGraph.from(this)).buildRoot()
  }

  lifecycle {
    close<WorkbenchViewModel>()
  }
}
