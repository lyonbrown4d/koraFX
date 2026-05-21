package dev.korafx.sample.navigation

import dev.korafx.navigation.PathRoute

sealed class WorkbenchRoute(
    override val id: String,
    override val title: String,
    override val path: String,
    val summary: String,
    val documentResource: String,
) : PathRoute {
    data object Overview : WorkbenchRoute(
        id = "overview",
        title = "Overview",
        path = "/",
        summary = "This workbench is the complete demo application for KoraFX DSL, MVVM, theme and component modules.",
        documentResource = "dev/korafx/sample/docs/overview.md",
    )

    data object Dsl : WorkbenchRoute(
        id = "dsl",
        title = "DSL",
        path = "/dsl",
        summary = "The DSL layer makes JavaFX layout and controls easier to compose without hiding native JavaFX APIs.",
        documentResource = "dev/korafx/sample/docs/dsl.md",
    )

    data object Components : WorkbenchRoute(
        id = "components",
        title = "Components",
        path = "/components",
        summary = "The component package provides reusable workbench surfaces for editors, data grids, resources, tabs and command launchers.",
        documentResource = "dev/korafx/sample/docs/components.md",
    )

    data object Mvvm : WorkbenchRoute(
        id = "mvvm",
        title = "MVVM",
        path = "/mvvm",
        summary = "The MVVM layer is based on StateFlow and is wired by the framework through Koin-friendly constructor injection.",
        documentResource = "dev/korafx/sample/docs/mvvm.md",
    )

    data object Theme : WorkbenchRoute(
        id = "theme",
        title = "Theme",
        path = "/theme",
        summary = "KoraFX themes are selectable JavaFX stylesheet presets generated from typed tokens.",
        documentResource = "dev/korafx/sample/docs/theme.md",
    )

    companion object {
        val all: List<WorkbenchRoute>
            get() = listOf(Overview, Dsl, Components, Mvvm, Theme)
    }
}
