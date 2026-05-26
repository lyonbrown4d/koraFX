package dev.korafx.navigation

import dev.korafx.components.borderLayout
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RouterHostFeatureTest {
    @Test
    fun `router host renders named outlets inside a layout`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all)

        try {
            val host = routerHostFor(scope, navigator) {
                layout(TestLayout.Workbench) {
                    shellWithOutlets { outlets ->
                        borderLayout {
                            center(outlets.primary)
                            right(outlets.outlet("details"))
                        }
                    }
                    routeView(TestRoute.Home) {
                        primary { route -> Label("page:${route.id}") }
                        outlet("details") { route -> Label("details:${route.id}") }
                    }
                    route(TestRoute.Settings) { route -> Label("page:${route.id}") }
                }
            }

            FxTestSupport.waitForFxCondition {
                host.currentRouterPageText() == "page:home" && host.routerOutletText("details") == "details:home"
            }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                host.currentRouterPageText() == "page:settings" && host.routerOutletText("details") == null
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host provides navigation location to route views`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(PathComponentRoute.Home, PathComponentRoute.all)

        try {
            val host = routerHostFor(scope, navigator) {
                route(PathComponentRoute.Home) { route -> Label(route.title) }
                routeView(PathComponentRoute.Project) {
                    primaryWithLocation { context ->
                        Label("project:${context.params["projectId"]}:${context.query["tab"]}")
                    }
                }
            }

            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == "Home" }
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
        val navigator = Navigator(TestRoute.Home, TestRoute.all)
        val module = RouterModule<TestRoute> { router ->
            router.route(TestRoute.Home) { route -> Label("module:${route.id}") }
            router.route(TestRoute.Settings) { route -> Label("module:${route.id}") }
        }

        try {
            val host = routerHostFor(scope, navigator) { include(module) }

            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == "module:home" }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == "module:settings" }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host initializes lazy routes only when first rendered`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all)
        var initialized = 0

        try {
            val host = routerHostFor(scope, navigator) {
                route(TestRoute.Home) { route -> Label("page:${route.id}") }
                routeLazy(TestRoute.Settings) {
                    initialized += 1
                    { route -> Label("lazy:${route.id}") }
                }
            }

            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == "page:home" }
            assertEquals(0, initialized)
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == "lazy:settings" }
            assertEquals(1, initialized)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host rejects routes referencing unknown layouts`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        try {
            assertFailsWith<IllegalArgumentException> {
                routerHost(scope = scope, navigator = Navigator(TestRoute.Home, TestRoute.all)) {
                    route(TestRoute.Home, layout = TestLayout.Workbench) { route -> Label(route.title) }
                }
            }
        } finally {
            scope.cancel()
        }
    }

    private fun <R : Route> routerHostFor(
        scope: CoroutineScope,
        navigator: Navigator<R>,
        block: RouterHostBuilder<R>.() -> Unit,
    ): StackPane = FxTestSupport.run {
        lateinit var result: StackPane
        runOnFxThread {
            result = routerHost(scope, navigator, content = block)
        }
        result
    }
}
