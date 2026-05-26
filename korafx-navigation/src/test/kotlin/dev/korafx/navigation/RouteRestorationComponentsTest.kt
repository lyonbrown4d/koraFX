package dev.korafx.navigation

import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.ScrollPane
import javafx.scene.control.TableView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.test.Test

class RouteRestorationComponentsTest {
    @Test
    fun `scroll pane restores route scroll values`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all)

        try {
            val scrollPane = FxTestSupport.run {
                lateinit var result: ScrollPane
                runOnFxThread {
                    result = ScrollPane(Label("Content")).apply {
                        routeScrollRestoration(scope, navigator)
                    }
                }
                result
            }

            FxTestSupport.runOnFxThread {
                scrollPane.vvalue = 0.75
                scrollPane.hvalue = 0.25
            }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition { scrollPane.vvalue == 0.0 && scrollPane.hvalue == 0.0 }
            FxTestSupport.runOnFxThread {
                scrollPane.vvalue = 0.10
                scrollPane.hvalue = 0.20
            }
            navigator.navigate(TestRoute.Home.id)

            FxTestSupport.waitForFxCondition { scrollPane.vvalue == 0.75 && scrollPane.hvalue == 0.25 }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `list and table views restore route selections`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all)

        try {
            val (listView, tableView) = FxTestSupport.run {
                lateinit var listView: ListView<String>
                lateinit var tableView: TableView<String>
                runOnFxThread {
                    listView = ListView<String>().apply {
                        items.setAll("home", "settings")
                        routeSelectionRestoration(scope, navigator)
                    }
                    tableView = TableView<String>().apply {
                        items.setAll("home", "settings")
                        routeSelectionRestoration(scope, navigator)
                    }
                }
                listView to tableView
            }

            FxTestSupport.runOnFxThread {
                listView.selectionModel.select("home")
                tableView.selectionModel.select("home")
            }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                listView.selectionModel.selectedItem == null && tableView.selectionModel.selectedItem == null
            }
            FxTestSupport.runOnFxThread {
                listView.selectionModel.select("settings")
                tableView.selectionModel.select("settings")
            }
            navigator.navigate(TestRoute.Home.id)

            FxTestSupport.waitForFxCondition {
                listView.selectionModel.selectedItem == "home" && tableView.selectionModel.selectedItem == "home"
            }
        } finally {
            scope.cancel()
        }
    }
}
