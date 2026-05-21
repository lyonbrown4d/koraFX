package dev.korafx.sample.ui

import dev.korafx.sample.navigation.WorkbenchRoute
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons

object WorkbenchIcons {
    val NextTheme: BootstrapIcons = BootstrapIcons.PALETTE
    val Commands: BootstrapIcons = BootstrapIcons.COMMAND
    val Stable: BootstrapIcons = BootstrapIcons.CHECK_CIRCLE
    val Theme: BootstrapIcons = BootstrapIcons.MOON_STARS
    val Dsl: BootstrapIcons = BootstrapIcons.BRACES
    val Samples: BootstrapIcons = BootstrapIcons.COLLECTION
    val Connected: BootstrapIcons = BootstrapIcons.LIGHTNING_CHARGE
    val Overlay: BootstrapIcons = BootstrapIcons.WINDOW_SIDEBAR
    val Warning: BootstrapIcons = BootstrapIcons.INFO_CIRCLE
    val Home: BootstrapIcons = BootstrapIcons.HOUSE
    val Route: BootstrapIcons = BootstrapIcons.SIGNPOST_SPLIT
    val Editor: BootstrapIcons = BootstrapIcons.FILE_CODE
    val Workspace: BootstrapIcons = BootstrapIcons.LAYOUT_SPLIT
    val Database: BootstrapIcons = BootstrapIcons.TABLE

    fun route(route: WorkbenchRoute): BootstrapIcons =
        when (route) {
            WorkbenchRoute.Overview -> BootstrapIcons.HOUSE
            WorkbenchRoute.Dsl -> BootstrapIcons.BRACES
            WorkbenchRoute.Components -> BootstrapIcons.BOX_SEAM
            WorkbenchRoute.Mvvm -> BootstrapIcons.DIAGRAM_3
            WorkbenchRoute.Theme -> BootstrapIcons.PALETTE
        }
}
