package dev.korafx.sample.data

import dev.korafx.sample.domain.SourceSnippet

internal fun coreWorkbenchSourceSnippets(): List<SourceSnippet> =
    listOf(
        SourceSnippet(
            id = "framework-entry",
            module = "Framework",
            title = "Application entry",
            description = "The framework entry owns window configuration, DI modules, theme presets and app content.",
            language = "kotlin",
            routeIds = setOf("overview", "framework"),
            code = """
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
                        default(BuiltInThemes.MaterialLight)
                    }

                    navigation {
                        initialRoute = WorkbenchRoute.Overview
                        routes(WorkbenchRoute.all)
                    }

                    content {
                        WorkbenchRootView(WorkbenchAppGraph.from(this)).buildRoot()
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "dsl-stateful-layout",
            module = "DSL",
            title = "State-bound JavaFX layout",
            description = "Compose normal controls and bind their text/value directly from StateFlow.",
            language = "kotlin",
            routeIds = setOf("overview", "dsl"),
            code = """
                val projectName = MutableStateFlow("KoraFX")

                vbox(spacing = 12.0) {
                    label("Project")
                    textField(projectName.value) {
                        textProperty().addListener { _, _, value ->
                            projectName.value = value
                        }
                    }
                    label().stateText(uiScope, projectName) { "Editing ${'$'}it" }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "mvvm-action-state",
            module = "MVVM",
            title = "ViewModel action and state",
            description = "Keep UI events as actions and expose renderable state through the ViewModel.",
            language = "kotlin",
            routeIds = setOf("overview", "mvvm"),
            code = """
                data class CounterState(val count: Int) : ViewState

                sealed interface CounterAction : UiAction {
                    data object Increment : CounterAction
                }

                class CounterViewModel : ViewModel<CounterState, CounterAction, UiEvent>(
                    CounterState(count = 0),
                ) {
                    override fun onAction(action: CounterAction) {
                        if (action is CounterAction.Increment) {
                            updateState { it.copy(count = it.count + 1) }
                        }
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "theme-scene-controller",
            module = "Theme",
            title = "Scene theme wiring",
            description = "Bind generated theme stylesheets once and drive changes through ThemeManager.",
            language = "kotlin",
            routeIds = setOf("overview", "theme"),
            code = """
                val themeManager = ThemeManager()
                val themeController = SceneThemeController(themeManager)

                stage.scene = Scene(root)
                themeController.bind(stage.scene)

                ghostButton("Next Theme") {
                    onAction { themeManager.nextTheme() }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "navigation-route-host",
            module = "Navigation",
            title = "Route navigation",
            description = "Use typed routes for tabs, rails, command palette actions and deep links.",
            language = "kotlin",
            routeIds = setOf("overview", "navigation", "components"),
            code = """
                sealed class AppRoute(
                    override val id: String,
                    override val path: String,
                ) : PathRoute {
                    data object Home : AppRoute("home", "/")
                    data object Settings : AppRoute("settings", "/settings")
                }

                val navigator = Navigator(AppRoute.Home, listOf(AppRoute.Home, AppRoute.Settings))
                navigator.navigatePath("/settings")
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "components-surface",
            module = "Components",
            title = "Surface composition",
            description = "Base components provide consistent page headers, cards, actions and semantic badges.",
            language = "kotlin",
            routeIds = setOf("components"),
            code = """
                pageHeader(
                    title = "Modules",
                    subtitle = "Reusable surface components for desktop tools.",
                    eyebrow = "korafx-components",
                )

                section("Actions") {
                    actionBar(alignEnd = false) {
                        button("Run")
                        ghostButton("Cancel")
                        badge("Ready", ComponentTone.SUCCESS)
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "source-editor-diagnostics",
            module = "SourceEditor",
            title = "Editor diagnostics",
            description = "Attach diagnostics and editor actions to a source preview without browser embedding.",
            language = "kotlin",
            routeIds = setOf("components", "source-editor"),
            code = """
                sourceEditor(
                    text = "fun main() = println(\"KoraFX\")",
                    language = "kotlin",
                ) {
                    diagnostics(
                        SourceDiagnostic(1, 14, "Replace preview target.", ComponentTone.INFO),
                    )
                    action("Format") {
                        println("Format current source")
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "data-grid-actions",
            module = "DataGrid",
            title = "Grid selection and snapshots",
            description = "Configure typed columns, selection summaries and snapshot actions.",
            language = "kotlin",
            routeIds = setOf("components", "data-grid"),
            code = """
                dataGrid(modules) {
                    constrainedResize()
                    readOnlyTextColumn("Name") { it.name }
                    readOnlyTextColumn("Owner") { it.owner }
                    readOnlyTextColumn("Status") { it.status }

                    selectionSummary()
                    toolbarSnapshotAction("Copy Selected", selectedOnly = true) { snapshot ->
                        val text = snapshot.toDelimitedText()
                        clipboard.putString(text)
                    }
                }
            """.trimIndent(),
        ),
    )
