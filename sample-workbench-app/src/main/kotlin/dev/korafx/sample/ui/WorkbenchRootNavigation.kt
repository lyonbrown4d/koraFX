package dev.korafx.sample.ui

import dev.korafx.components.section
import dev.korafx.dsl.button
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.scrollPane
import dev.korafx.dsl.sidebar
import dev.korafx.dsl.styleClasses
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.navigation.Navigator
import dev.korafx.navigation.routeButton
import dev.korafx.sample.domain.ModuleCategory
import dev.korafx.sample.navigation.WorkbenchRoute
import kotlinx.coroutines.CoroutineScope

internal fun workbenchModuleSidebar(
    uiScope: CoroutineScope,
    navigator: Navigator<WorkbenchRoute>,
) =
    scrollPane(
        init = {
            prefWidth = 300.0
            minWidth = 220.0
            maxWidth = 480.0
            isFitToWidth = true
        },
    ) {
        content {
            sidebar(width = 278.0, spacing = 12.0) {
                label("Modules") {
                    styleClasses(ThemeStyleClass.Headline)
                }

                add(
                    routeButton(
                        scope = uiScope,
                        navigator = navigator,
                        route = WorkbenchRoute.Overview,
                        text = "Overview",
                    ) {
                        maxWidth = Double.MAX_VALUE
                    },
                )

                ModuleCategory.entries.forEach { category ->
                    label(category.title) {
                        styleClasses(ThemeStyleClass.Muted)
                    }

                    WorkbenchRoute.moduleRoutes
                        .filter { route -> WorkbenchRoute.findModule(route.id)?.category == category }
                        .forEach { route ->
                            add(
                                routeButton(
                                    scope = uiScope,
                                    navigator = navigator,
                                    route = route,
                                    text = route.title,
                                ) {
                                    maxWidth = Double.MAX_VALUE
                                },
                            )
                        }
                }

                section("Quick Actions") {
                    button("随机跳转") {
                        onAction {
                            navigator.navigatePath("/components/source-editor")
                        }
                    }
                    button("清理历史") {
                        onAction {
                            navigator.clearNavigationHistory()
                        }
                    }
                }
            }
        }
    }
