package dev.korafx.sample

import dev.korafx.navigation.Route

sealed class WorkbenchRoute(
    override val id: String,
    override val title: String,
    val summary: String,
    val document: String,
) : Route {
    data object Overview : WorkbenchRoute(
        id = "overview",
        title = "Overview",
        summary = "KoraFX is kept intentionally small: Kotlin DSL, MVVM helpers, and a few optional JavaFX components.",
        document =
            """
            KoraFX Lean Baseline

            The sample now starts from plain JavaFX Application and wires dependencies by hand.
            This keeps the library independent from any DI container or application framework.

            Kept modules:
            - framework-dsl
            - framework-state
            - framework-mvvm
            - framework-navigation
            - framework-theme
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

    data object Mvvm : WorkbenchRoute(
        id = "mvvm",
        title = "MVVM",
        summary = "The MVVM layer is based on StateFlow and does not depend on Koin, Dagger, Spring, or any other DI library.",
        document =
            """
            MVVM Layer

            ViewModel owns state and events.
            The application decides how ViewModels are created:
            - manual construction
            - Koin
            - Dagger
            - custom factory

            KoraFX should not couple MVVM to a DI container.
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
            get() = listOf(Overview, Dsl, Mvvm, Theme)
    }
}
