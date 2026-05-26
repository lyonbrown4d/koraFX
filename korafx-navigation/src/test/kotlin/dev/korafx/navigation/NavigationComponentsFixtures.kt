package dev.korafx.navigation

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope

internal data class TestRoute(
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

internal data class TransitionMetaRoute(
    override val id: String,
    override val title: String,
    override val path: String,
    override val meta: RouteMeta = RouteMeta.Empty,
) : PathRoute {
    companion object {
        val Source = TransitionMetaRoute(id = "source", title = "Source", path = "/")
        val CustomTransition = TransitionMetaRoute(
            id = "custom-transition",
            title = "CustomTransition",
            path = "/custom",
            meta = routeMeta(ROUTE_TRANSITION_META_KEY to "fade"),
        )

        val all: List<TransitionMetaRoute>
            get() = listOf(Source, CustomTransition)
    }
}

internal enum class TestLayout {
    Workbench,
    Details,
}

internal data class PathComponentRoute(
    override val id: String,
    override val title: String,
    override val path: String,
) : PathRoute {
    companion object {
        val Home = PathComponentRoute("home", "Home", "/")
        val Project = PathComponentRoute("project", "Project", "/projects/:projectId")

        val all: List<PathComponentRoute>
            get() = listOf(Home, Project)
    }
}

internal fun VBox.labels(): List<String> =
    children.map { node -> (node as Label).text }

internal fun StackPane.currentRouterPage(): Node? {
    val node = children.singleOrNull() ?: return null
    val layout = node as? BorderPane
    val outlet = layout?.center as? StackPane
    return outlet?.children?.singleOrNull() ?: node
}

internal fun StackPane.currentRouterPageText(): String? =
    (currentRouterPage() as? Label)?.text

internal fun StackPane.routerOutletText(name: String): String? {
    val layout = children.singleOrNull() as? BorderPane
    val outlet = when (name) {
        "details" -> layout?.right as? StackPane
        else -> null
    }
    return (outlet?.children?.singleOrNull() as? Label)?.text
}

internal fun <R : Route> routeHostFor(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    content: (R) -> Node,
): StackPane =
    FxTestSupport.run {
        lateinit var result: StackPane
        runOnFxThread {
            result = routeHost(scope, navigator, content = content)
        }
        result
    }
