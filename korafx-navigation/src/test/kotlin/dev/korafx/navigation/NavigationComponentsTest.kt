package dev.korafx.navigation

import dev.korafx.dsl.RenderState
import dev.korafx.components.borderLayout
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.ScrollPane
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class NavigationComponentsTest {
    @Test
    fun `navigation rail renders routes and updates active button`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )

        try {
            val rail = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.VBox
                runOnFxThread {
                    result = navigationRail(
                        scope = scope,
                        navigator = navigator,
                        icon = { BootstrapIcons.ALARM },
                    )
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                rail.children.size == 2 &&
                    (rail.children[0] as Button).styleClass.contains("nav-button-active")
            }
            assertEquals(
                BootstrapIcons.ALARM,
                assertIs<FontIcon>((rail.children[0] as Button).graphic).iconCode,
            )

            navigator.navigate(TestRoute.Settings.id)

            FxTestSupport.waitForFxCondition {
                !(rail.children[0] as Button).styleClass.contains("nav-button-active") &&
                    (rail.children[1] as Button).styleClass.contains("nav-button-active")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `navigation rail uses async navigation guards`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )
        val guardCalls = AtomicInteger(0)
        navigator.beforeEnterAsync(TestRoute.Settings) {
            guardCalls.incrementAndGet()
            dev.korafx.navigation.NavigationDecision.Block("Settings disabled")
        }

        try {
            val rail = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.VBox
                runOnFxThread {
                    result = navigationRail(
                        scope = scope,
                        navigator = navigator,
                    )
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                rail.children.size == 2
            }
            FxTestSupport.runOnFxThread {
                (rail.children[1] as Button).fire()
            }

            FxTestSupport.waitForFxCondition {
                guardCalls.get() == 1
            }
            assertEquals(TestRoute.Home, navigator.currentRoute)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `path buttons navigate by path and update active state`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = PathComponentRoute.Home,
            routes = PathComponentRoute.all,
        )

        try {
            val button = FxTestSupport.run {
                lateinit var result: Button
                runOnFxThread {
                    result = pathButton(
                        scope = scope,
                        navigator = navigator,
                        path = "/projects/42?tab=files",
                        text = "Project",
                    )
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                button.styleClass.contains("route-button-inactive")
            }
            FxTestSupport.runOnFxThread {
                button.fire()
            }

            FxTestSupport.waitForFxCondition {
                navigator.currentRoute == PathComponentRoute.Project &&
                    navigator.currentLocation.params["projectId"] == "42" &&
                    button.styleClass.contains("route-button-active")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route links navigate by route and expose route link state`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )
        var activeState: Boolean? = null

        try {
            val link = FxTestSupport.run {
                lateinit var result: Hyperlink
                runOnFxThread {
                    result = routeLink(
                        scope = scope,
                        navigator = navigator,
                        route = TestRoute.Settings,
                    ) { state ->
                        activeState = state.active
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                activeState == false && link.styleClass.contains("route-link-inactive")
            }
            FxTestSupport.runOnFxThread {
                link.fire()
            }

            FxTestSupport.waitForFxCondition {
                navigator.currentRoute == TestRoute.Settings &&
                    activeState == true &&
                    link.styleClass.contains("route-link-active")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route host recreates nodes for recreate policy`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
            pageInstancePolicy = PageInstancePolicy.RECREATE,
        )
        var created = 0

        try {
            val host = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.StackPane
                runOnFxThread {
                    result = routeHost(scope, navigator) { route ->
                        created += 1
                        Label(route.title)
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                host.children.singleOrNull() is Label
            }

            val firstHome = host.children.single()
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == TestRoute.Settings.title
            }
            navigator.navigate(TestRoute.Home.id)
            FxTestSupport.waitForFxCondition {
                host.children.singleOrNull() !== firstHome &&
                    (host.children.singleOrNull() as? Label)?.text == TestRoute.Home.title
            }

            assertEquals(3, created)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route host reuses nodes for keep alive policy`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
            pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
        )

        try {
            val host = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.StackPane
                runOnFxThread {
                    result = routeHost(scope, navigator) { route ->
                        Label(route.title)
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == TestRoute.Home.title
            }
            val firstHome = host.children.single()

            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == TestRoute.Settings.title
            }
            navigator.navigate(TestRoute.Home.id)
            FxTestSupport.waitForFxCondition {
                host.children.singleOrNull() === firstHome
            }

            assertSame(firstHome, host.children.single())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host renders routes inside a shared layout outlet`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )
        var layoutCreated = 0

        try {
            val host = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.StackPane
                runOnFxThread {
                    result = routerHost(scope, navigator) {
                        layout(TestLayout.Workbench) {
                            shell { outlet ->
                                layoutCreated += 1
                                borderLayout {
                                    top(Label("Workbench"))
                                    center(outlet)
                                }
                            }
                            route(TestRoute.Home) { route ->
                                Label("page:${route.id}")
                            }
                            route(TestRoute.Settings) { route ->
                                Label("page:${route.id}")
                            }
                        }
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                host.currentRouterPageText() == "page:home"
            }

            val firstLayout = host.children.single()
            navigator.navigate(TestRoute.Settings.id)

            FxTestSupport.waitForFxCondition {
                host.children.single() === firstLayout &&
                    host.currentRouterPageText() == "page:settings"
            }

            assertEquals(1, layoutCreated)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host can switch between layout routes and direct routes`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )

        try {
            val host = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.StackPane
                runOnFxThread {
                    result = routerHost(scope, navigator) {
                        layout(TestLayout.Workbench) {
                            shell { outlet ->
                                borderLayout {
                                    top(Label("Workbench"))
                                    center(outlet)
                                }
                            }
                            route(TestRoute.Home) { route ->
                                Label("layout:${route.id}")
                            }
                        }
                        route(TestRoute.Settings) { route ->
                            Label("direct:${route.id}")
                        }
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                host.currentRouterPageText() == "layout:home"
            }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "direct:settings"
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host keeps route pages alive inside layouts`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
            pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
        )

        try {
            val host = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.StackPane
                runOnFxThread {
                    result = routerHost(scope, navigator) {
                        layout(TestLayout.Workbench) {
                            shell { outlet ->
                                borderLayout {
                                    center(outlet)
                                }
                            }
                            route(TestRoute.Home) { route ->
                                Label(route.title)
                            }
                            route(TestRoute.Settings) { route ->
                                Label(route.title)
                            }
                        }
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                host.currentRouterPageText() == TestRoute.Home.title
            }
            val firstHome = host.currentRouterPage()

            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                host.currentRouterPageText() == TestRoute.Settings.title
            }
            navigator.navigate(TestRoute.Home.id)
            FxTestSupport.waitForFxCondition {
                host.currentRouterPage() === firstHome
            }

            assertSame(firstHome, host.currentRouterPage())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host renders named outlets inside a layout`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )

        try {
            val host = FxTestSupport.run {
                lateinit var result: StackPane
                runOnFxThread {
                    result = routerHost(scope, navigator) {
                        layout(TestLayout.Workbench) {
                            shellWithOutlets { outlets ->
                                borderLayout {
                                    center(outlets.primary)
                                    right(outlets.outlet("details"))
                                }
                            }
                            routeView(TestRoute.Home) {
                                primary { route ->
                                    Label("page:${route.id}")
                                }
                                outlet("details") { route ->
                                    Label("details:${route.id}")
                                }
                            }
                            route(TestRoute.Settings) { route ->
                                Label("page:${route.id}")
                            }
                        }
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                host.currentRouterPageText() == "page:home" &&
                    host.routerOutletText("details") == "details:home"
            }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                host.currentRouterPageText() == "page:settings" &&
                    host.routerOutletText("details") == null
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host renders nested layout chains`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Settings,
            routes = TestRoute.all,
        )

        try {
            val host = FxTestSupport.run {
                lateinit var result: StackPane
                runOnFxThread {
                    result = routerHost(scope, navigator) {
                        layout(TestLayout.Workbench) {
                            shell { outlet ->
                                borderLayout {
                                    top(Label("root-layout"))
                                    center(outlet)
                                }
                            }
                            layout(TestLayout.Details) {
                                shell { outlet ->
                                    borderLayout {
                                        top(Label("child-layout"))
                                        center(outlet)
                                    }
                                }
                                route(TestRoute.Settings) { route ->
                                    Label("page:${route.id}")
                                }
                            }
                        }
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                val root = host.children.singleOrNull() as? BorderPane
                val child = (root?.center as? StackPane)?.children?.singleOrNull() as? BorderPane
                val page = (child?.center as? StackPane)?.children?.singleOrNull() as? Label
                (root?.top as? Label)?.text == "root-layout" &&
                    (child?.top as? Label)?.text == "child-layout" &&
                    page?.text == "page:settings"
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host provides navigation location to route views`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = PathComponentRoute.Home,
            routes = PathComponentRoute.all,
        )

        try {
            val host = FxTestSupport.run {
                lateinit var result: StackPane
                runOnFxThread {
                    result = routerHost(scope, navigator) {
                        route(PathComponentRoute.Home) { route ->
                            Label(route.title)
                        }
                        routeView(PathComponentRoute.Project) {
                            primaryWithLocation { context ->
                                Label(
                                    "project:${context.params["projectId"]}:${context.query["tab"]}",
                                )
                            }
                        }
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "Home"
            }
            navigator.navigatePath("/projects/42?tab=files")
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "project:42:files"
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host can include route modules`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )
        val module = RouterModule<TestRoute> { router ->
            router.route(TestRoute.Home) { route ->
                Label("module:${route.id}")
            }
            router.route(TestRoute.Settings) { route ->
                Label("module:${route.id}")
            }
        }

        try {
            val host = FxTestSupport.run {
                lateinit var result: StackPane
                runOnFxThread {
                    result = routerHost(scope, navigator) {
                        include(module)
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "module:home"
            }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "module:settings"
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host initializes lazy routes only when first rendered`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )
        var initialized = 0

        try {
            val host = FxTestSupport.run {
                lateinit var result: StackPane
                runOnFxThread {
                    result = routerHost(scope, navigator) {
                        route(TestRoute.Home) { route ->
                            Label("page:${route.id}")
                        }
                        routeLazy(TestRoute.Settings) {
                            initialized += 1
                            { route -> Label("lazy:${route.id}") }
                        }
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "page:home"
            }
            assertEquals(0, initialized)

            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "lazy:settings"
            }
            assertEquals(1, initialized)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route data host renders loaded route data`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )

        try {
            val host = FxTestSupport.run {
                lateinit var result: StackPane
                runOnFxThread {
                    result = routeDataHost(
                        scope = scope,
                        navigator = navigator,
                        load = { context -> "data:${context.route.id}" },
                        loading = { Label("loading") },
                    ) { _, value ->
                        Label(value)
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "data:home"
            }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "data:settings"
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route data host can cache data and revalidate on demand`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )
        val controller = RouteDataController()
        var loads = 0

        try {
            val host = FxTestSupport.run {
                lateinit var result: StackPane
                runOnFxThread {
                    result = routeDataHost(
                        scope = scope,
                        navigator = navigator,
                        controller = controller,
                        cache = true,
                        load = { context ->
                            loads += 1
                            "data:${context.route.id}:$loads"
                        },
                        loading = { Label("loading") },
                    ) { _, value ->
                        Label(value)
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "data:home:1"
            }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "data:settings:2"
            }
            navigator.navigate(TestRoute.Home.id)
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "data:home:1"
            }

            controller.revalidate()
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "data:home:3"
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `scroll pane restores route scroll values`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )

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
            FxTestSupport.waitForFxCondition {
                scrollPane.vvalue == 0.0 && scrollPane.hvalue == 0.0
            }
            FxTestSupport.runOnFxThread {
                scrollPane.vvalue = 0.10
                scrollPane.hvalue = 0.20
            }
            navigator.navigate(TestRoute.Home.id)

            FxTestSupport.waitForFxCondition {
                scrollPane.vvalue == 0.75 && scrollPane.hvalue == 0.25
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `list and table views restore route selections`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )

        try {
            val views = FxTestSupport.run {
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
            val listView = views.first
            val tableView = views.second

            FxTestSupport.runOnFxThread {
                listView.selectionModel.select("home")
                tableView.selectionModel.select("home")
            }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                listView.selectionModel.selectedItem == null &&
                    tableView.selectionModel.selectedItem == null
            }
            FxTestSupport.runOnFxThread {
                listView.selectionModel.select("settings")
                tableView.selectionModel.select("settings")
            }
            navigator.navigate(TestRoute.Home.id)

            FxTestSupport.waitForFxCondition {
                listView.selectionModel.selectedItem == "home" &&
                    tableView.selectionModel.selectedItem == "home"
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host rejects routes referencing unknown layouts`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        try {
            assertFailsWith<IllegalArgumentException> {
                routerHost(
                    scope = scope,
                    navigator = Navigator(TestRoute.Home, TestRoute.all),
                ) {
                    route(TestRoute.Home, layout = TestLayout.Workbench) { route ->
                        Label(route.title)
                    }
                }
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route state host renders state for current route`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )
        val home = MutableStateFlow<RenderState<List<String>>>(RenderState.Loading)
        val settings = MutableStateFlow<RenderState<List<String>>>(RenderState.Empty)

        try {
            val host = FxTestSupport.run {
                lateinit var result: VBox
                runOnFxThread {
                    result = routeStateHost(
                        scope = scope,
                        navigator = navigator,
                        stateFor = { route ->
                            when (route) {
                                TestRoute.Home -> home
                                TestRoute.Settings -> settings
                                else -> error("Unexpected route: ${route.id}")
                            }
                        },
                        loading = { route ->
                            label("loading:${route.id}")
                        },
                        empty = { route ->
                            label("empty:${route.id}")
                        },
                        failed = { route, failure ->
                            label("failed:${route.id}:${failure.message}")
                        },
                    ) { route, rows ->
                        rows.forEach { row ->
                            label("${route.id}:$row")
                        }
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("loading:home")
            }

            home.value = RenderState.Content(listOf("alpha", "beta"))
            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("home:alpha", "home:beta")
            }

            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("empty:settings")
            }

            settings.value = RenderState.Failed("Offline")
            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("failed:settings:Offline")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route state host ignores stale route state updates after navigation`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )
        val home = MutableStateFlow<RenderState<List<String>>>(
            RenderState.Content(listOf("home-initial")),
        )
        val settings = MutableStateFlow<RenderState<List<String>>>(
            RenderState.Content(listOf("settings-initial")),
        )

        try {
            val host = FxTestSupport.run {
                lateinit var result: VBox
                runOnFxThread {
                    result = routeStateHost(
                        scope = scope,
                        navigator = navigator,
                        stateFor = { route ->
                            when (route) {
                                TestRoute.Home -> home
                                TestRoute.Settings -> settings
                                else -> error("Unexpected route: ${route.id}")
                            }
                        },
                    ) { route, rows ->
                        rows.forEach { row ->
                            label("${route.id}:$row")
                        }
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("home:home-initial")
            }

            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("settings:settings-initial")
            }

            home.value = RenderState.Content(listOf("home-stale"))

            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("settings:settings-initial")
            }
        } finally {
            scope.cancel()
        }
    }

    private data class TestRoute(
        override val id: String,
        override val title: String,
    ) : Route {
        companion object {
            val Home = TestRoute(id = "home", title = "Home")
            val Settings = TestRoute(id = "settings", title = "Settings")

            val all: List<TestRoute>
                get() = listOf(Home, Settings)
        }
    }

    private fun VBox.labels(): List<String> =
        children.map { node -> (node as Label).text }

    private fun javafx.scene.layout.StackPane.currentRouterPage(): javafx.scene.Node? {
        val node = children.singleOrNull() ?: return null
        val layout = node as? BorderPane
        val outlet = layout?.center as? StackPane
        return outlet?.children?.singleOrNull() ?: node
    }

    private fun javafx.scene.layout.StackPane.currentRouterPageText(): String? =
        (currentRouterPage() as? Label)?.text

    private fun StackPane.routerOutletText(name: String): String? {
        val layout = children.singleOrNull() as? BorderPane
        val outlet = when (name) {
            "details" -> layout?.right as? StackPane
            else -> null
        }
        return (outlet?.children?.singleOrNull() as? Label)?.text
    }

    private enum class TestLayout {
        Workbench,
        Details,
    }

    private data class PathComponentRoute(
        override val id: String,
        override val title: String,
        override val path: String,
    ) : dev.korafx.navigation.PathRoute {
        companion object {
            val Home = PathComponentRoute("home", "Home", "/")
            val Project = PathComponentRoute("project", "Project", "/projects/:projectId")

            val all: List<PathComponentRoute>
                get() = listOf(Home, Project)
        }
    }
}
