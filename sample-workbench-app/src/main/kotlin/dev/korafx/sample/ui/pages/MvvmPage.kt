package dev.korafx.sample.ui.pages

import dev.korafx.components.actionBar
import dev.korafx.components.section
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClasses
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.sample.viewmodel.WorkbenchAction
import dev.korafx.sample.viewmodel.WorkbenchState

fun NodeContainerBuilder.mvvmPage(
    context: WorkbenchPageContext,
    state: WorkbenchState,
) {
    section(
        title = "MVVM state loop",
        description = "Actions enter the ViewModel, StateFlow renders the view, events update transient feedback.",
    ) {
        label("Count: ${state.mvvmCount}") {
            styleClasses(ThemeStyleClass.Headline)
        }
        actionBar(alignEnd = false) {
            button("-1") {
                onAction {
                    context.viewModel.dispatch(WorkbenchAction.DecrementCounter)
                }
            }
            button("+1") {
                onAction {
                    context.viewModel.dispatch(WorkbenchAction.IncrementCounter)
                }
            }
            ghostButton("Reset") {
                onAction {
                    context.viewModel.dispatch(WorkbenchAction.ResetCounter)
                }
            }
        }
    }
}
