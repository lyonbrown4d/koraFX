package dev.korafx.sample

import dev.korafx.components.actionBar
import dev.korafx.components.card
import dev.korafx.components.emptyState
import dev.korafx.components.navigationRail
import dev.korafx.components.section
import dev.korafx.dsl.bindDisable
import dev.korafx.dsl.bindEach
import dev.korafx.dsl.bindInvalid
import dev.korafx.dsl.bindList
import dev.korafx.dsl.bindSelectedItemBidirectional
import dev.korafx.dsl.bindText
import dev.korafx.dsl.bindTextBidirectional
import dev.korafx.dsl.bindValueBidirectional
import dev.korafx.dsl.bindVisible
import dev.korafx.dsl.choiceBox
import dev.korafx.dsl.comboBox
import dev.korafx.dsl.datePicker
import dev.korafx.dsl.form
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.gridPane
import dev.korafx.dsl.growVertical
import dev.korafx.dsl.intSpinner
import dev.korafx.dsl.menuBar
import dev.korafx.dsl.onAction
import dev.korafx.dsl.panel
import dev.korafx.dsl.scrollPane
import dev.korafx.dsl.statusBar
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.tableView
import dev.korafx.dsl.treeView
import dev.korafx.dsl.toolbar
import dev.korafx.dsl.vbox
import dev.korafx.dsl.workbenchLayout
import dev.korafx.navigation.Navigator
import dev.korafx.theme.SceneThemeController
import dev.korafx.theme.ThemeManager
import javafx.application.Application
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
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
            lateinit var titleLabel: Label
            lateinit var summaryLabel: Label
            lateinit var documentArea: TextArea
            lateinit var themeLabel: Label
            lateinit var feedbackLabel: Label
            lateinit var eventLabel: Label
            lateinit var dslDemo: VBox
            lateinit var mvvmDemo: VBox
            lateinit var counterLabel: Label
            lateinit var draftField: TextField
            lateinit var submitButton: Button
            lateinit var notesBox: VBox
            lateinit var statusBox: HBox

            topBar {
                toolbar {
                    label("KoraFX") {
                        styleClasses("headline")
                    }
                    spacer()
                    ghostButton("Toggle Theme") {
                        onAction {
                            viewModel.dispatch(WorkbenchAction.ToggleTheme)
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
                            titleLabel = label {
                                styleClasses("headline")
                            }
                            summaryLabel = label {
                                isWrapText = true
                                styleClasses("muted")
                            }
                            documentArea = textArea {
                                isEditable = false
                                prefRowCount = 16
                            }
                            themeLabel = label {
                                styleClasses("muted")
                            }
                            feedbackLabel = label {
                                styleClasses("muted")
                            }
                            eventLabel = label {
                                styleClasses("muted")
                            }
                            dslDemo = vbox(spacing = 16.0) {
                                menuBar {
                                    menu("DSL Actions") {
                                        actionItem("Toggle Theme") {
                                            viewModel.dispatch(WorkbenchAction.ToggleTheme)
                                        }
                                        actionItem("Open MVVM Route") {
                                            viewModel.dispatch(WorkbenchAction.Navigate(WorkbenchRoute.Mvvm.id))
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
                            }
                            mvvmDemo = vbox(spacing = 16.0) {
                                section(
                                    title = "StateFlow-backed ViewModel",
                                    description = "Buttons dispatch actions. The ViewModel updates state and emits feedback events.",
                                ) {
                                    counterLabel = label {
                                        styleClasses("headline")
                                    }

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
                                        draftField = textField(1, 0, "") {
                                            promptText = "Type a note for the ViewModel"
                                            maxWidth = Double.MAX_VALUE
                                            textProperty().addListener { _, _, newValue ->
                                                val nextValue = newValue.orEmpty()
                                                if (viewModel.state.value.mvvmDraft != nextValue) {
                                                    viewModel.dispatch(WorkbenchAction.UpdateDraft(nextValue))
                                                }
                                            }
                                        }

                                        label(0, 1, "Notes")
                                        cell(1, 1, horizontalGrow = Priority.ALWAYS) {
                                            vbox(
                                                spacing = 10.0,
                                                init = {
                                                    minHeight = 150.0
                                                },
                                            ) {
                                            }.also {
                                                notesBox = it
                                            }
                                        }
                                    }

                                    actionBar {
                                        ghostButton("Clear Notes") {
                                            onAction {
                                                viewModel.dispatch(WorkbenchAction.ClearNotes)
                                            }
                                        }
                                        submitButton = button("Submit Draft") {
                                            onAction {
                                                viewModel.dispatch(WorkbenchAction.SubmitDraft)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            footer {
                statusBar {}.also {
                    statusBox = it
                }
            }

            bindUi(
                titleLabel = titleLabel,
                summaryLabel = summaryLabel,
                documentArea = documentArea,
                themeLabel = themeLabel,
                feedbackLabel = feedbackLabel,
                eventLabel = eventLabel,
                dslDemo = dslDemo,
                mvvmDemo = mvvmDemo,
                counterLabel = counterLabel,
                draftField = draftField,
                submitButton = submitButton,
                notesBox = notesBox,
                statusBox = statusBox,
            )
        }

    private fun bindUi(
        titleLabel: Label,
        summaryLabel: Label,
        documentArea: TextArea,
        themeLabel: Label,
        feedbackLabel: Label,
        eventLabel: Label,
        dslDemo: VBox,
        mvvmDemo: VBox,
        counterLabel: Label,
        draftField: TextField,
        submitButton: Button,
        notesBox: VBox,
        statusBox: HBox,
    ) {
        val state = viewModel.state

        titleLabel.bindText(uiScope, state.map { it.title })
        summaryLabel.bindText(uiScope, state.map { it.summary })
        documentArea.bindText(uiScope, state.map { it.document })
        themeLabel.bindText(uiScope, state.map { "Theme: ${it.currentThemeName}" })
        feedbackLabel.bindText(uiScope, state.map { "State: ${it.feedbackMessage}" })
        eventLabel.bindText(
            uiScope,
            viewModel.events.map { event ->
                when (event) {
                    is WorkbenchEvent.Feedback -> "Last event: ${event.message}"
                }
            },
        )
        dslDemo.bindVisible(uiScope, state.map { it.currentRouteId == WorkbenchRoute.Dsl.id })
        mvvmDemo.bindVisible(uiScope, state.map { it.currentRouteId == WorkbenchRoute.Mvvm.id })
        counterLabel.bindText(uiScope, state.map { "Count: ${it.mvvmCount}" })
        draftField.bindText(uiScope, state.map { it.mvvmDraft })
        submitButton.bindDisable(uiScope, state.map { it.mvvmDraft.isBlank() })
        notesBox.bindList(
            scope = uiScope,
            flow = state.map { it.mvvmNotes },
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

        statusBox.bindEach(uiScope, state.map { it.statusItems }) { item ->
            label(item) {
                styleClasses("muted")
            }
        }
    }

    override fun stop() {
        uiScope.cancel()
        viewModel.close()
        themeController.dispose()
    }
}
