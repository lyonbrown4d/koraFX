package dev.korafx.navigation

import dev.korafx.dsl.styleClass
import javafx.scene.Node
import javafx.scene.layout.StackPane

class RouterOutlets internal constructor() {
    private val outletMap = linkedMapOf<String, StackPane>()

    val primary: StackPane
        get() = outlet(PRIMARY_OUTLET)

    fun outlet(name: String): StackPane {
        require(name.isNotBlank()) {
            "Router outlet name cannot be blank."
        }

        return outletMap.getOrPut(name) {
            StackPane().apply {
                styleClass("router-layout-outlet")
                styleClass("router-layout-outlet-${name.toStyleClassSuffix()}")
            }
        }
    }

    internal fun render(
        primaryNode: Node,
        namedNodes: Map<String, Node>,
    ) {
        primary.children.setAll(primaryNode)
        outletMap
            .filterKeys { it != PRIMARY_OUTLET }
            .forEach { (name, outlet) ->
                val node = namedNodes[name]
                if (node == null) {
                    outlet.children.clear()
                } else {
                    outlet.children.setAll(node)
                }
            }
    }

    companion object {
        internal const val PRIMARY_OUTLET = "primary"
    }
}

internal data class RouterHostGraph<R : Route>(
    val routes: Map<String, RouterRouteView<R>>,
    val layouts: Map<Any, RouterLayoutView>,
    val fallback: ((R) -> Node)?,
)

internal data class RouterRouteView<R : Route>(
    val layoutKey: Any?,
    val primary: (context: RouterViewContext<R>) -> Node,
    val outlets: Map<String, (context: RouterViewContext<R>) -> Node>,
)

internal data class RouterLayoutView(
    val parentKey: Any?,
    val shellFactory: (outlets: RouterOutlets) -> Node,
)

internal data class RouterLayoutInstance(
    val outlets: RouterOutlets,
    val node: Node,
)

internal fun String.toStyleClassSuffix(): String =
    lowercase()
        .map { character ->
            when {
                character.isLetterOrDigit() -> character
                else -> '-'
            }
        }
        .joinToString("")
        .trim('-')
        .ifBlank { "unnamed" }
