package dev.korafx.sample

import dev.korafx.components.ComponentTone
import dev.korafx.components.statusItem
import dev.korafx.devtools.KoraDevtoolsLanguage
import dev.korafx.devtools.KoraDevtoolsPlacement
import dev.korafx.devtools.devtools
import dev.korafx.framework.koraApplication
import dev.korafx.framework.theme.BuiltInThemes
import dev.korafx.macos.installMacosChrome
import dev.korafx.macos.macos
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
    minSize(860.0, 560.0)
    titleBar {
      subtitle = "Framework + Components"
      cornerRadius = 14.0
      transparentBackground = true
      dragOpacity = 0.92
      macos {
        preserveTrafficLights = true
        fullSizeContentView = true
        transparentTitlebar = true
        trafficLightInset(14.0, 12.0)
      }
      content {
        statusItem("Custom titlebar slot", ComponentTone.INFO)
      }
    }
  }

  installKoin {
    modules(workbenchModule())
  }

  installMacosChrome()

  theme {
    presets(BuiltInThemes.all)
    default(BuiltInThemes.MaterialLight)
    persistSelection = true
  }

  navigation {
    initialRoute = WorkbenchRoute.Overview
    routes(WorkbenchRoute.all)
    persistLocation = true
  }

  devtools {
    enabled = true
    shortcut = "Ctrl+Shift+I"
    pickerShortcut = "Ctrl+Shift+C"
    highlightSelection = true
    language = KoraDevtoolsLanguage.SYSTEM
    placement = KoraDevtoolsPlacement.BOTTOM
    dockWidth = 420.0
    dockHeight = 360.0
  }

  content {
    WorkbenchRootView(WorkbenchAppGraph.from(this)).buildRoot()
  }

  lifecycle {
    close<WorkbenchViewModel>()
  }
}
