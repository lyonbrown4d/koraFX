package dev.korafx.examples.mvvm

import dev.korafx.dsl.bindDisable
import dev.korafx.dsl.bindText
import dev.korafx.dsl.cssStyle
import dev.korafx.dsl.hbox
import dev.korafx.dsl.onAction
import dev.korafx.dsl.vbox
import dev.korafx.framework.mvvm.UiAction
import dev.korafx.framework.mvvm.UiEvent
import dev.korafx.framework.mvvm.ViewModel
import dev.korafx.framework.mvvm.ViewState
import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.stage.Stage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map

fun main(args: Array<String>) {
    Application.launch(MvvmCounterApp::class.java, *args)
}

private data class CounterState(
    val count: Int = 0,
    val stepText: String = "1",
    val lastEvent: String = "Ready.",
) : ViewState {
    val step: Int
        get() = stepText.toIntOrNull()?.coerceAtLeast(1) ?: 1
}

private sealed interface CounterAction : UiAction {
    data object Increment : CounterAction
    data object Decrement : CounterAction
    data object Reset : CounterAction
    data class UpdateStep(val value: String) : CounterAction
}

private sealed interface CounterEvent : UiEvent {
    data class Changed(val message: String) : CounterEvent
}

private class CounterViewModel : ViewModel<CounterState, CounterAction, CounterEvent>(CounterState()) {
    override fun onAction(action: CounterAction) {
        when (action) {
            CounterAction.Increment -> changeBy(currentState.step)
            CounterAction.Decrement -> changeBy(-currentState.step)
            CounterAction.Reset -> {
                setState(currentState.copy(count = 0, lastEvent = "Reset to zero."))
                tryEmitEvent(CounterEvent.Changed("Reset to zero."))
            }
            is CounterAction.UpdateStep -> {
                updateState { it.copy(stepText = action.value.filter { char -> char.isDigit() }.ifBlank { "1" }) }
            }
        }
    }

    private fun changeBy(delta: Int) {
        val nextCount = currentState.count + delta
        val message = "Count changed to $nextCount."
        setState(currentState.copy(count = nextCount, lastEvent = message))
        tryEmitEvent(CounterEvent.Changed(message))
    }
}

class MvvmCounterApp : Application() {
    private val uiScope = MainScope()
    private val viewModel = CounterViewModel()

    override fun start(stage: Stage) {
        lateinit var stepField: TextField

        val root = vbox(
            spacing = 18.0,
            init = {
                padding = Insets(24.0)
                alignment = Pos.CENTER_LEFT
                cssStyle {
                    fontSize(14.0)
                }
            },
        ) {
            label("KoraFX MVVM Counter") {
                cssStyle {
                    fontSize(22.0)
                    fontWeight("bold")
                }
            }
            label {
                cssStyle {
                    fontSize(44.0)
                    fontWeight("bold")
                }
                bindText(uiScope, viewModel.state.map { it.count.toString() })
            }
            hbox(spacing = 10.0) {
                alignment(Pos.CENTER_LEFT)

                button("-") {
                    onAction {
                        viewModel.dispatch(CounterAction.Decrement)
                    }
                }
                button("+") {
                    onAction {
                        viewModel.dispatch(CounterAction.Increment)
                    }
                }
                button("Reset") {
                    bindDisable(uiScope, viewModel.state.map { it.count == 0 })
                    onAction {
                        viewModel.dispatch(CounterAction.Reset)
                    }
                }
            }
            hbox(spacing = 10.0) {
                alignment(Pos.CENTER_LEFT)

                label("Step")
                stepField = textField("1") {
                    prefColumnCount = 4
                    textProperty().addListener { _, _, value ->
                        viewModel.dispatch(CounterAction.UpdateStep(value.orEmpty()))
                    }
                }
            }
            label {
                cssStyle {
                    textFill("#5f6b7a")
                }
                bindText(uiScope, viewModel.state.map { it.lastEvent })
            }
        }

        stepField.bindText(uiScope, viewModel.state.map { it.stepText })

        stage.title = "KoraFX MVVM Counter"
        stage.scene = Scene(root, 520.0, 360.0)
        stage.show()
    }

    override fun stop() {
        uiScope.cancel()
        viewModel.close()
    }
}
