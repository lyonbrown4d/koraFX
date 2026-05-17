package dev.korafx.sample

import dev.korafx.components.actionBar
import dev.korafx.components.card
import dev.korafx.components.emptyState
import dev.korafx.components.navigationRail
import dev.korafx.components.section
import dev.korafx.dsl.accordion
import dev.korafx.dsl.bindInvalid
import dev.korafx.dsl.bindSelectedItem
import dev.korafx.dsl.bindSelectedItemBidirectional
import dev.korafx.dsl.bindTextBidirectional
import dev.korafx.dsl.bindValueBidirectional
import dev.korafx.dsl.choiceBox
import dev.korafx.dsl.colorPicker
import dev.korafx.dsl.comboBox
import dev.korafx.dsl.datePicker
import dev.korafx.dsl.form
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.gridPane
import dev.korafx.dsl.growVertical
import dev.korafx.dsl.hyperlink
import dev.korafx.dsl.intSpinner
import dev.korafx.dsl.listView
import dev.korafx.dsl.menuButton
import dev.korafx.dsl.menuBar
import dev.korafx.dsl.onAction
import dev.korafx.dsl.panel
import dev.korafx.dsl.pagination
import dev.korafx.dsl.passwordField
import dev.korafx.dsl.progressBar
import dev.korafx.dsl.radioButton
import dev.korafx.dsl.scrollPane
import dev.korafx.dsl.slider
import dev.korafx.dsl.stateDisable
import dev.korafx.dsl.stateList
import dev.korafx.dsl.stateText
import dev.korafx.dsl.stateVisible
import dev.korafx.dsl.statusBar
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.splitMenuButton
import dev.korafx.dsl.tableView
import dev.korafx.dsl.tabPane
import dev.korafx.dsl.toggleButton
import dev.korafx.dsl.treeView
import dev.korafx.dsl.toolbar
import dev.korafx.dsl.vbox
import dev.korafx.dsl.workbenchLayout
import dev.korafx.navigation.Navigator
import dev.korafx.theme.KoraTheme
import dev.korafx.theme.SceneThemeController
import dev.korafx.theme.ThemeManager
import javafx.application.Application
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private data class ModuleSummary(
    val name: String,
    val responsibility: String,
)

class SampleWorkbenchApp : Application() {
    private val moduleSummaries = listOf(
        ModuleSummary("framework-dsl", "Kotlin-first JavaFX construction API"),
        ModuleSummary("framework-mvvm", "StateFlow ViewModel helpers without DI coupling"),
        ModuleSummary("framework-components", "Optional reusable JavaFX components"),
        ModuleSummary("framework-theme", "Selectable JavaFX theme presets from typed tokens"),
    )
    private val dslProjectName = MutableStateFlow("KoraFX")
    private val dslProjectNameError = dslProjectName.map { value ->
        if (value.isBlank()) "Project name is required." else null
    }
    private val dslModeOptions = listOf("DSL First", "MVVM Ready", "Component Polish")
    private val dslRuntimeOptions = listOf("Manual JavaFX", "Custom Factory", "External DI")
    private val dslMode = MutableStateFlow<String?>("DSL First")
    private val dslRuntime = MutableStateFlow<String?>("Manual JavaFX")
    private val dslParallelism = MutableStateFlow(2)
    private val dslTargetDate = MutableStateFlow<LocalDate?>(LocalDate.now().plusWeeks(1))

    private val uiScope = MainScope()
    private val themeManager = ThemeManager()
    private val navigator = Navigator(
        initialRoute = WorkbenchRoute.Overview,
        routes = WorkbenchRoute.all,
    )
    private val viewModel = WorkbenchViewModel(themeManager, navigator)
    private val themeController = SceneThemeController(themeManager)

    override fun start(stage: Stage) {
        val scene = Scene(buildRoot(), 1120.0, 720.0)
        themeController.bind(scene)

        stage.title = "KoraFX Lean Workbench"
        stage.scene = scene
        stage.show()
    }

    private fun buildRoot() =
        workbenchLayout {
            lateinit var feedbackLabel: Label

            topBar {
                toolbar {
                    label("KoraFX") {
                        styleClasses("headline")
                    }
                    spacer()
                    comboBox<KoraTheme>(
                        items = themeManager.availableThemes,
                        init = {
                            prefWidth = 180.0
                        },
                    ) {
                        render { it.displayName }
                        onSelect { theme ->
                            if (theme != null) {
                                viewModel.dispatch(WorkbenchAction.SelectTheme(theme.id))
                            }
                        }
                    }.bindSelectedItem(uiScope, themeManager.theme)
                    ghostButton("Next Theme") {
                        onAction {
                            viewModel.dispatch(WorkbenchAction.NextTheme)
                        }
                    }
                }
            }

            navigation {
                navigationRail(uiScope, navigator)
            }

            content {
                scrollPane(
                    init = {
                        isFitToWidth = true
                    },
                ) {
                    content {
                        panel {
                            label {
                                styleClasses("headline")
                            }.stateText(uiScope, viewModel.state) { it.title }
                            label {
                                isWrapText = true
                                styleClasses("muted")
                            }.stateText(uiScope, viewModel.state) { it.summary }
                            textArea {
                                isEditable = false
                                prefRowCount = 16
                            }.stateText(uiScope, viewModel.state) { it.document }
                            label {
                                styleClasses("muted")
                            }.stateText(uiScope, viewModel.state) { "Theme: ${it.currentThemeName}" }
                            feedbackLabel = label {
                                styleClasses("muted")
                            }.stateText(uiScope, viewModel.state) { "State: ${it.feedbackMessage}" }
                            label {
                                styleClasses("muted")
                            }.stateText(uiScope, viewModel.events) { event ->
                                when (event) {
                                    is WorkbenchEvent.Feedback -> "Last event: ${event.message}"
                                }
                            }
                            vbox(spacing = 16.0) {
                                menuBar {
                                    menu("DSL Actions") {
                                        actionItem("Toggle Theme") {
                                            viewModel.dispatch(WorkbenchAction.ToggleTheme)
                                        }
                                        actionItem("Open MVVM Route") {
                                            viewModel.dispatch(WorkbenchAction.Navigate(WorkbenchRoute.Mvvm.id))
                                        }
                                        actionItem("Open Theme Route") {
                                            viewModel.dispatch(WorkbenchAction.Navigate(WorkbenchRoute.Theme.id))
                                        }
                                    }
                                }

                                section(
                                    title = "Surface Components",
                                    description = "section, card and actionBar are lightweight containers built on top of the DSL.",
                                ) {
                                    gridPane(
                                        hgap = 12.0,
                                        vgap = 10.0,
                                    ) {
                                        column(prefWidth = 120.0, alignment = HPos.RIGHT)
                                        column(grow = Priority.ALWAYS, fillWidth = true)

                                        label(0, 0, "Project")
                                        textField(1, 0, "KoraFX") {
                                            maxWidth = Double.MAX_VALUE
                                        }

                                        label(0, 1, "Mode")
                                        checkBox(1, 1, "Kotlin-first JavaFX DSL") {
                                            isSelected = true
                                        }
                                    }

                                    actionBar {
                                        ghostButton("Secondary") {
                                            onAction {
                                                feedbackLabel.text = "State: Secondary action from actionBar."
                                            }
                                        }
                                        button("Primary") {
                                            onAction {
                                                feedbackLabel.text = "State: Primary action from actionBar."
                                            }
                                        }
                                    }
                                }

                                section(
                                    title = "Form And Table DSL",
                                    description = "Common controls stay close to JavaFX while removing repetitive layout code.",
                                ) {
                                    form {
                                        lateinit var projectField: TextField

                                        item(
                                            label = "Project",
                                            helper = "Validation is just Flow binding; no form model is required.",
                                        ) {
                                            projectField = textField {
                                                maxWidth = Double.MAX_VALUE
                                                bindTextBidirectional(uiScope, dslProjectName)
                                                bindInvalid(
                                                    uiScope,
                                                    dslProjectNameError.map { it != null },
                                                )
                                            }
                                            validationMessage(uiScope, dslProjectNameError)
                                        }

                                        checkBox(
                                            label = "Options",
                                            text = "Keep MVVM independent from DI",
                                        ) {
                                            isSelected = true
                                        }

                                        item(
                                            label = "Mode",
                                            helper = "ComboBox selection can bind directly to MutableStateFlow.",
                                        ) {
                                            comboBox(
                                                items = dslModeOptions,
                                                init = {
                                                    maxWidth = Double.MAX_VALUE
                                                },
                                            ) {
                                                select("DSL First")
                                            }.bindSelectedItemBidirectional(uiScope, dslMode)
                                        }

                                        item(label = "Runtime") {
                                            choiceBox(items = dslRuntimeOptions) {
                                                select("Manual JavaFX")
                                            }.bindSelectedItemBidirectional(uiScope, dslRuntime)
                                        }

                                        item(label = "Parallel Work") {
                                            intSpinner(
                                                min = 1,
                                                max = 8,
                                                initialValue = dslParallelism.value,
                                            ) {
                                                isEditable = true
                                                bindValueBidirectional(uiScope, dslParallelism)
                                            }
                                        }

                                        item(label = "Target Date") {
                                            datePicker {
                                                bindValueBidirectional(uiScope, dslTargetDate)
                                            }
                                        }

                                        submitBar {
                                            secondaryButton("Reset") {
                                                onAction {
                                                    dslProjectName.value = "KoraFX"
                                                    dslMode.value = "DSL First"
                                                    dslRuntime.value = "Manual JavaFX"
                                                    dslParallelism.value = 2
                                                    dslTargetDate.value = LocalDate.now().plusWeeks(1)
                                                }
                                            }
                                            primaryButton("Apply") {
                                                onAction {
                                                    val project = projectField.text.trim()
                                                    feedbackLabel.text =
                                                        if (project.isEmpty()) {
                                                            "State: Form validation blocked submit."
                                                        } else {
                                                            "State: $project uses ${dslMode.value} with ${dslParallelism.value} workers."
                                                        }
                                                }
                                            }
                                        }
                                    }

                                    tableView(
                                        items = moduleSummaries,
                                        init = {
                                            prefHeight = 180.0
                                            maxWidth = Double.MAX_VALUE
                                            growVertical(Priority.SOMETIMES)
                                        },
                                    ) {
                                        constrainedResize()
                                        placeholder("No modules")
                                        textColumn("Module") { it.name }
                                        textColumn("Responsibility") { it.responsibility }
                                        actionColumn(title = "Action", text = "Inspect") { row ->
                                            feedbackLabel.text = "State: Inspect ${row.name}."
                                        }
                                    }
                                }

                                card {
                                    label("Composable TreeView") {
                                        styleClasses("headline")
                                    }
                                    treeView<String>(
                                        init = {
                                            prefHeight = 150.0
                                        },
                                    ) {
                                        root("DSL Coverage") {
                                            item("Layouts")
                                            item("Controls")
                                            item("Bindings")
                                            item("Menus")
                                            item("Components")
                                        }
                                        showRoot(true)
                                        render { "• $it" }
                                        rowAction { item ->
                                            feedbackLabel.text = "State: Tree row action: $item"
                                        }
                                    }
                                }

                                emptyState(
                                    title = "Feedback components",
                                    message = "emptyState, loadingState and errorState are plain JavaFX nodes.",
                                    actionText = "Mark Reviewed",
                                    onAction = {
                                        feedbackLabel.text = "State: Feedback component action."
                                    },
                                ) {
                                    prefHeight = 180.0
                                }
                            }.stateVisible(uiScope, viewModel.state) {
                                it.currentRouteId == WorkbenchRoute.Dsl.id
                            }
                            vbox(spacing = 16.0) {
                                section(
                                    title = "StateFlow-backed ViewModel",
                                    description = "Buttons dispatch actions. The ViewModel updates state and emits feedback events.",
                                ) {
                                    label {
                                        styleClasses("headline")
                                    }.stateText(uiScope, viewModel.state) { "Count: ${it.mvvmCount}" }

                                    actionBar(alignEnd = false) {
                                        alignment(Pos.CENTER_LEFT)

                                        button("-1") {
                                            onAction {
                                                viewModel.dispatch(WorkbenchAction.DecrementCounter)
                                            }
                                        }
                                        button("+1") {
                                            onAction {
                                                viewModel.dispatch(WorkbenchAction.IncrementCounter)
                                            }
                                        }
                                        ghostButton("Reset") {
                                            onAction {
                                                viewModel.dispatch(WorkbenchAction.ResetCounter)
                                            }
                                        }
                                    }
                                }

                                section(
                                    title = "Action / Event Flow",
                                    description = "Text input updates ViewModel state. Submit emits a one-shot event and renders notes from state.",
                                ) {
                                    gridPane(
                                        hgap = 12.0,
                                        vgap = 10.0,
                                    ) {
                                        column(prefWidth = 120.0, alignment = HPos.RIGHT)
                                        column(grow = Priority.ALWAYS, fillWidth = true)

                                        label(0, 0, "Draft")
                                        textField(1, 0, "") {
                                            promptText = "Type a note for the ViewModel"
                                            maxWidth = Double.MAX_VALUE
                                        }.stateText(
                                            scope = uiScope,
                                            state = viewModel.state,
                                            onTextChange = { nextValue ->
                                                viewModel.dispatch(WorkbenchAction.UpdateDraft(nextValue))
                                            }
                                        ) { it.mvvmDraft }

                                        label(0, 1, "Notes")
                                        cell(1, 1, horizontalGrow = Priority.ALWAYS) {
                                            vbox(
                                                spacing = 10.0,
                                                init = {
                                                    minHeight = 150.0
                                                },
                                            ) {
                                            }.stateList(
                                                scope = uiScope,
                                                state = viewModel.state,
                                                items = { it.mvvmNotes },
                                                empty = {
                                                    emptyState(
                                                        title = "No notes yet",
                                                        message = "Submit the draft to prove state-driven rendering.",
                                                    ) {
                                                        prefHeight = 130.0
                                                    }
                                                },
                                            ) { note ->
                                                ghostButton(note) {
                                                    maxWidth = Double.MAX_VALUE
                                                    onAction {
                                                        viewModel.dispatch(WorkbenchAction.RecallNote(note))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    actionBar {
                                        ghostButton("Clear Notes") {
                                            onAction {
                                                viewModel.dispatch(WorkbenchAction.ClearNotes)
                                            }
                                        }
                                        button("Submit Draft") {
                                            onAction {
                                                viewModel.dispatch(WorkbenchAction.SubmitDraft)
                                            }
                                        }.stateDisable(uiScope, viewModel.state) {
                                            it.mvvmDraft.isBlank()
                                        }
                                    }
                                }
                            }.stateVisible(uiScope, viewModel.state) {
                                it.currentRouteId == WorkbenchRoute.Mvvm.id
                            }
                            vbox(spacing = 16.0) {
                                section(
                                    title = "Theme Presets",
                                    description = "Choose any built-in theme. The Scene stylesheet is regenerated from typed tokens.",
                                ) {
                                    gridPane(
                                        hgap = 12.0,
                                        vgap = 10.0,
                                    ) {
                                        column(prefWidth = 120.0, alignment = HPos.RIGHT)
                                        column(grow = Priority.ALWAYS, fillWidth = true)

                                        label(0, 0, "Preset")
                                        cell(1, 0, horizontalGrow = Priority.ALWAYS) {
                                            comboBox<KoraTheme>(
                                                items = themeManager.availableThemes,
                                                init = {
                                                    maxWidth = Double.MAX_VALUE
                                                },
                                            ) {
                                                render { it.displayName }
                                                onSelect { theme ->
                                                    if (theme != null) {
                                                        viewModel.dispatch(WorkbenchAction.SelectTheme(theme.id))
                                                    }
                                                }
                                            }.also {
                                                it.bindSelectedItem(uiScope, themeManager.theme)
                                            }
                                        }

                                        label(0, 1, "Current")
                                        label(1, 1) {
                                            styleClasses("muted")
                                        }.stateText(uiScope, themeManager.theme) { "${it.displayName} (${it.id})" }
                                    }

                                    actionBar(alignEnd = false) {
                                        ghostButton("Previous") {
                                            onAction {
                                                viewModel.dispatch(WorkbenchAction.PreviousTheme)
                                            }
                                        }
                                        button("Next") {
                                            onAction {
                                                viewModel.dispatch(WorkbenchAction.NextTheme)
                                            }
                                        }
                                        ghostButton("Light / Dark") {
                                            onAction {
                                                viewModel.dispatch(WorkbenchAction.ToggleTheme)
                                            }
                                        }
                                    }
                                }

                                section(
                                    title = "Current Theme Tokens",
                                    description = "These values are the source for generated JavaFX CSS.",
                                ) {
                                    gridPane(
                                        hgap = 12.0,
                                        vgap = 10.0,
                                    ) {
                                        column(prefWidth = 120.0, alignment = HPos.RIGHT)
                                        column(grow = Priority.ALWAYS, fillWidth = true)

                                        label(0, 0, "Primary")
                                        label(1, 0).stateText(uiScope, themeManager.theme) { it.tokens.colors.primary }
                                        label(0, 1, "Surface")
                                        label(1, 1).stateText(uiScope, themeManager.theme) { it.tokens.colors.surface }
                                        label(0, 2, "Muted Surface")
                                        label(1, 2).stateText(uiScope, themeManager.theme) { it.tokens.colors.surfaceMuted }
                                        label(0, 3, "Text")
                                        label(1, 3).stateText(uiScope, themeManager.theme) {
                                            "${it.tokens.colors.textPrimary} / ${it.tokens.colors.textSecondary}"
                                        }
                                        label(0, 4, "Semantic")
                                        label(1, 4).stateText(uiScope, themeManager.theme) {
                                            "success ${it.tokens.colors.success}, warning ${it.tokens.colors.warning}, danger ${it.tokens.colors.danger}, info ${it.tokens.colors.info}"
                                        }
                                        label(0, 5, "Typography")
                                        label(1, 5).stateText(uiScope, themeManager.theme) {
                                            "${it.tokens.typography.fontFamily}, ${it.tokens.typography.baseSize}px / ${it.tokens.typography.headlineSize}px"
                                        }
                                        label(0, 6, "Radius")
                                        label(1, 6).stateText(uiScope, themeManager.theme) { "${it.tokens.radius}px" }
                                        label(0, 7, "Radii")
                                        label(1, 7).stateText(uiScope, themeManager.theme) {
                                            "small ${it.tokens.radii.small}px, medium ${it.tokens.radii.medium}px, large ${it.tokens.radii.large}px"
                                        }
                                        label(0, 8, "Spacing")
                                        label(1, 8).stateText(uiScope, themeManager.theme) {
                                            "sm ${it.tokens.spacing.sm}px, md ${it.tokens.spacing.md}px, xl ${it.tokens.spacing.xl}px"
                                        }
                                        label(0, 9, "States")
                                        label(1, 9).stateText(uiScope, themeManager.theme) {
                                            "hover ${it.tokens.states.surfaceHover}, selected ${it.tokens.states.selected}, disabled ${it.tokens.states.disabledOpacity}"
                                        }
                                    }
                                }

                                section(
                                    title = "Theme Control Gallery",
                                    description = "Switch presets above and check whether common JavaFX controls keep a consistent surface, radius and interaction style.",
                                ) {
                                    vbox(spacing = 16.0) {
                                        flowPane(
                                            hgap = 10.0,
                                            vgap = 10.0,
                                        ) {
                                            button("Primary")
                                            ghostButton("Ghost")
                                            toggleButton("Toggle") {
                                                isSelected = true
                                            }
                                            menuButton("Menu") {
                                                actionItem("Refresh") {
                                                    feedbackLabel.text = "State: Menu action."
                                                }
                                                actionItem("Export") {
                                                    feedbackLabel.text = "State: Export action."
                                                }
                                            }
                                            splitMenuButton("Split Action") {
                                                actionItem("Run now") {
                                                    feedbackLabel.text = "State: Split action."
                                                }
                                                actionItem("Schedule") {
                                                    feedbackLabel.text = "State: Schedule action."
                                                }
                                            }
                                            hyperlink("Documentation") {
                                                onAction {
                                                    feedbackLabel.text = "State: Hyperlink action."
                                                }
                                            }
                                        }

                                        gridPane(
                                            hgap = 12.0,
                                            vgap = 10.0,
                                        ) {
                                            val toneGroup = javafx.scene.control.ToggleGroup()

                                            column(prefWidth = 120.0, alignment = HPos.RIGHT)
                                            column(grow = Priority.ALWAYS, fillWidth = true)
                                            column(grow = Priority.ALWAYS, fillWidth = true)

                                            label(0, 0, "Text")
                                            textField(1, 0, "Editable value") {
                                                maxWidth = Double.MAX_VALUE
                                            }
                                            cell(2, 0, horizontalGrow = Priority.ALWAYS) {
                                                passwordField("secret") {
                                                    maxWidth = Double.MAX_VALUE
                                                }
                                            }

                                            label(0, 1, "Selection")
                                            cell(1, 1, horizontalGrow = Priority.ALWAYS) {
                                                comboBox(
                                                    items = listOf("Kotlin", "JavaFX", "StateFlow"),
                                                    init = {
                                                        maxWidth = Double.MAX_VALUE
                                                    },
                                                ) {
                                                    select("Kotlin")
                                                }
                                            }
                                            cell(2, 1, horizontalGrow = Priority.ALWAYS) {
                                                choiceBox(
                                                    items = listOf("Light", "Dark", "Brand"),
                                                    init = {
                                                        maxWidth = Double.MAX_VALUE
                                                    },
                                                ) {
                                                    select("Brand")
                                                }
                                            }

                                            label(0, 2, "Pickers")
                                            cell(1, 2, horizontalGrow = Priority.ALWAYS) {
                                                datePicker(LocalDate.now()) {
                                                    maxWidth = Double.MAX_VALUE
                                                }
                                            }
                                            cell(2, 2, horizontalGrow = Priority.ALWAYS) {
                                                colorPicker(Color.web("#246BFD")) {
                                                    maxWidth = Double.MAX_VALUE
                                                }
                                            }

                                            label(0, 3, "Inputs")
                                            cell(1, 3, horizontalGrow = Priority.ALWAYS) {
                                                intSpinner(
                                                    min = 1,
                                                    max = 12,
                                                    initialValue = 4,
                                                ) {
                                                    isEditable = true
                                                    maxWidth = Double.MAX_VALUE
                                                }
                                            }
                                            cell(2, 3, horizontalGrow = Priority.ALWAYS) {
                                                hbox(spacing = 12.0) {
                                                    checkBox("Enabled") {
                                                        isSelected = true
                                                    }
                                                    radioButton("A", toneGroup) {
                                                        isSelected = true
                                                    }
                                                    radioButton("B", toneGroup)
                                                }
                                            }
                                        }

                                        tabPane {
                                            tab("Controls") {
                                                vbox(
                                                    spacing = 10.0,
                                                    init = {
                                                        prefHeight = 160.0
                                                    },
                                                ) {
                                                    label("Progress")
                                                    progressBar(0.68) {
                                                        maxWidth = Double.MAX_VALUE
                                                    }
                                                    label("Slider")
                                                    slider(
                                                        min = 0.0,
                                                        max = 100.0,
                                                        value = 68.0,
                                                    ) {
                                                        maxWidth = Double.MAX_VALUE
                                                    }
                                                }
                                            }
                                            tab("Navigation") {
                                                accordion {
                                                    pane("Accordion Pane", expanded = true) {
                                                        vbox(spacing = 8.0) {
                                                            label("Accordion, titled panes and tabs share the same theme tokens.")
                                                            pagination(
                                                                pageCount = 3,
                                                                init = {
                                                                    maxPageIndicatorCount = 3
                                                                },
                                                            ) { pageIndex ->
                                                                Label("Preview page ${pageIndex + 1}").apply {
                                                                    styleClasses("muted")
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            tab("Lists") {
                                                listView(
                                                    items = listOf("List item", "Selected row style", "Hover row style"),
                                                    init = {
                                                        prefHeight = 160.0
                                                        selectionModel.select("Selected row style")
                                                    },
                                                ) {
                                                    render { it }
                                                }
                                            }
                                        }
                                    }
                                }

                                section(
                                    title = "Built-In Theme Catalog",
                                    description = "The catalog is plain data, so applications can expose all presets or only a curated subset.",
                                ) {
                                    tableView(
                                        items = themeManager.availableThemes,
                                        init = {
                                            prefHeight = 240.0
                                            maxWidth = Double.MAX_VALUE
                                            growVertical(Priority.SOMETIMES)
                                        },
                                    ) {
                                        constrainedResize()
                                        textColumn("Theme") { it.displayName }
                                        textColumn("ID") { it.id }
                                        textColumn("Primary") { it.tokens.colors.primary }
                                        textColumn("Radius") { "${it.tokens.radius}px" }
                                        actionColumn(title = "Action", text = "Use") { theme ->
                                            viewModel.dispatch(WorkbenchAction.SelectTheme(theme.id))
                                        }
                                    }
                                }
                            }.stateVisible(uiScope, viewModel.state) {
                                it.currentRouteId == WorkbenchRoute.Theme.id
                            }
                        }
                    }
                }
            }

            footer {
                statusBar {
                }.stateList(
                    scope = uiScope,
                    state = viewModel.state,
                    items = { it.statusItems },
                ) { item ->
                    label(item) {
                        styleClasses("muted")
                    }
                }
            }
        }

    override fun stop() {
        uiScope.cancel()
        viewModel.close()
        themeController.dispose()
    }
}
