package dev.korafx.examples.navigationtheme

import dev.korafx.components.ToastTone
import dev.korafx.components.actionBar
import dev.korafx.dsl.bindText
import dev.korafx.dsl.button
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.panel
import dev.korafx.dsl.scrollPane
import dev.korafx.dsl.stateText
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.textArea
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.navigation.NavigationDecision
import dev.korafx.navigation.bindContentWithTransition
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal fun NavigationThemeApp.documentationPane() =
    scrollPane(
        init = {
            isFitToWidth = true
        },
    ) {
        content {
            panel(spacing = 12.0, padding = 14.0) {
                label("文档") {
                    styleClasses(ThemeStyleClass.Headline)
                }
            }.also { container ->
                container.bindContentWithTransition(
                    scope = uiScope,
                    state = navigator.state,
                    transition = transitionByState,
                ) { state ->
                    markdownDocument(state.currentRoute.documentation())
                }
            }
        }
    }

internal fun NavigationThemeApp.sourcePane() =
    scrollPane(
        init = {
            isFitToWidth = true
        },
    ) {
        content {
            panel(spacing = 12.0, padding = 14.0) {
                label("源码片段") {
                    styleClasses(ThemeStyleClass.Headline)
                }
                val source = textArea {
                    isEditable = false
                    isWrapText = false
                    prefRowCount = 24
                    this.styleClass += "source-editor-code"
                }
                source.bindText(
                    uiScope,
                    navigator.state.map { state -> state.currentRoute.sourceCode() }.distinctUntilChanged(),
                )
            }
        }
    }

internal fun NavigationThemeApp.stateSnapshotPane() =
    scrollPane(
        init = {
            isFitToWidth = true
        },
    ) {
        content {
            panel(spacing = 14.0, padding = 14.0) {
                label("当前导航状态") {
                    styleClasses(ThemeStyleClass.Headline)
                }
                label("Current route").also { label ->
                    label.stateText(uiScope, navigator.state) { state ->
                        "Current route: ${state.currentRoute.title}"
                    }
                }
                label("Current path").also { label ->
                    label.stateText(uiScope, navigator.state) { state ->
                        "Current path: ${state.currentLocation.fullPath}"
                    }
                }
                label("Navigation type").also { label ->
                    label.stateText(uiScope, navigator.state) { state ->
                        "Navigation type: ${state.navigationType.name}"
                    }
                }
                label("Back stack").also { label ->
                    label.stateText(uiScope, navigator.state) { state ->
                        "Back stack: ${state.backStack.size}"
                    }
                }
                label("Forward stack").also { label ->
                    label.stateText(uiScope, navigator.state) { state ->
                        "Forward stack: ${state.forwardStack.size}"
                    }
                }
                actionBar(alignEnd = false) {
                    button("Can Navigate") {
                        onAction {
                            val decision = navigator.canNavigate(DemoRoute.Overview.id)
                            val message =
                                when (decision) {
                                    is NavigationDecision.Allow -> "允许导航到 Overview"
                                    is NavigationDecision.Redirect -> "重定向到: ${decision.path ?: decision.routeId}"
                                    is NavigationDecision.Block -> decision.reason ?: "Navigation blocked"
                                }
                            notifications.show(
                                message = message,
                                tone = ToastTone.INFO,
                            )
                        }
                    }
                    button("Can Navigate Path /routes/42/files") {
                        onAction {
                            val decision = navigator.canNavigatePath("/routes/42/files")
                            if (decision == null) {
                                notifications.show(
                                    message = "No route can match /routes/42/files",
                                    tone = ToastTone.WARNING,
                                )
                            } else {
                                notifications.show(
                                    message = when (decision) {
                                        is NavigationDecision.Allow -> "Path 路由可导航"
                                        is NavigationDecision.Redirect -> "Redirect to ${decision.path}"
                                        is NavigationDecision.Block -> decision.reason ?: "blocked"
                                    },
                                    tone = ToastTone.INFO,
                                )
                            }
                        }
                    }
                    button("Pop To Root") {
                        onAction {
                            if (navigator.popToRoot()) {
                                notifications.show(
                                    message = "已回到起始路由",
                                    tone = ToastTone.SUCCESS,
                                )
                            } else {
                                notifications.show(
                                    message = "已经在起始路由",
                                    tone = ToastTone.INFO,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

private fun DemoRoute.documentation(): String = routeResourceText(docResource, fallback = "# Missing documentation\n")

private fun DemoRoute.sourceCode(): String = routeResourceText(sourceResource, fallback = "// Missing source snippet\n")

private val routeDocCache = linkedMapOf<String, String>()

private fun routeResourceText(
    resource: String,
    fallback: String,
): String {
    return routeDocCache.getOrPut(resource) {
        NavigationThemeApp::class.java.classLoader
            .getResourceAsStream(resource)
            ?.bufferedReader()
            ?.readText()
            ?: fallback
    }
}
