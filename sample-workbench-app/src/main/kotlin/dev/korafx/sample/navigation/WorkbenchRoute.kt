package dev.korafx.sample.navigation

import dev.korafx.framework.navigation.Route

sealed class WorkbenchRoute(
    override val id: String,
    override val title: String,
    val summary: String,
    val document: String,
) : Route {
    data object Overview : WorkbenchRoute(
        id = "overview",
        title = "Overview",
        summary = "This workbench is the complete demo application for KoraFX DSL, MVVM, theme and component modules.",
        document =
            """
            KoraFX Workbench Demo

            This sample is intentionally structured as a real desktop application:
            - Application owns only JavaFX lifecycle
            - di.WorkbenchAppGraph is the composition root
            - data/domain packages provide demo data and models
            - viewmodel owns StateFlow-backed state and actions
            - ui renders JavaFX nodes through the KoraFX DSL and components

            Demonstrated modules:
            - korafx-dsl
            - korafx-framework
            - korafx-components
            """.trimIndent(),
    )

    data object Dsl : WorkbenchRoute(
        id = "dsl",
        title = "DSL",
        summary = "The DSL layer makes JavaFX layout and controls easier to compose without hiding native JavaFX APIs.",
        document =
            """
            DSL Layer

            The DSL is the lowest-level user-facing capability.
            It should stay thin and Kotlin-friendly:
            - layout builders
            - control builders
            - binding helpers
            - styling helpers

            Native JavaFX nodes remain directly accessible.
            """.trimIndent(),
    )

    data object Components : WorkbenchRoute(
        id = "components",
        title = "Components",
        summary = "The component package provides reusable workbench surfaces for editors, data grids, resources, tabs and command launchers.",
        document =
            """
            Component Layer

            Components are optional, composable JavaFX nodes:
            - workbench and border layouts
            - source editor and query editor
            - resource explorer
            - tab workspace
            - data grid and editable table
            - inspector panel
            - activity timeline
            - command palette

            Applications can use these pieces independently or compose them into a full desktop tool.
            """.trimIndent(),
    )

    data object Mvvm : WorkbenchRoute(
        id = "mvvm",
        title = "MVVM",
        summary = "The MVVM layer is based on StateFlow and is wired by the framework through Koin-friendly constructor injection.",
        document =
            """
            MVVM Layer

            ViewModel owns state and events.
            korafx-framework provides a Koin composition module for application services.
            ViewModels stay constructor-injection friendly, so tests can still instantiate them directly.
            """.trimIndent(),
    )

    data object Theme : WorkbenchRoute(
        id = "theme",
        title = "Theme",
        summary = "KoraFX themes are selectable JavaFX stylesheet presets generated from typed tokens.",
        document =
            """
            Theme Layer

            ThemeManager owns the active KoraTheme as StateFlow.
            SceneThemeController binds a Scene to generated JavaFX CSS.

            Built-in themes are intentionally plain values:
            - display name
            - color tokens
            - typography tokens
            - radius

            Applications can expose all presets, a subset, or custom tokens.
            """.trimIndent(),
    )

    companion object {
        val all: List<WorkbenchRoute>
            get() = listOf(Overview, Dsl, Components, Mvvm, Theme)
    }
}
