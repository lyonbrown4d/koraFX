package dev.korafx.dsl

import dev.korafx.dsl.state.collectLatestIn
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.layout.Pane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface RenderState<out T> {
    data object Loading : RenderState<Nothing>
    data object Empty : RenderState<Nothing>

    data class Content<T>(
        val value: T,
    ) : RenderState<T>

    data class Failed(
        val message: String,
        val cause: Throwable? = null,
    ) : RenderState<Nothing>

    companion object {
        fun <T> loading(): RenderState<T> = Loading

        fun <T> empty(): RenderState<T> = Empty

        fun <T> content(value: T): RenderState<T> = Content(value)

        fun <T> failed(
            message: String,
            cause: Throwable? = null,
        ): RenderState<T> = Failed(message, cause)
    }
}

fun <T> List<T>.asRenderState(): RenderState<List<T>> =
    if (isEmpty()) {
        RenderState.Empty
    } else {
        RenderState.Content(this)
    }

fun <T> Flow<List<T>>.asRenderState(): Flow<RenderState<List<T>>> =
    map { items -> items.asRenderState() }

private fun runOnFxThread(block: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        block()
    } else {
        Platform.runLater {
            block()
        }
    }
}

private fun Pane.replaceChildren(content: FragmentBuilder.() -> Unit) {
    runOnFxThread {
        children.setAll(fragment(content))
    }
}

class FragmentBuilder internal constructor() : NodeContainerBuilder() {
    private val nodes = mutableListOf<Node>()

    override fun append(node: Node) {
        nodes += node
    }

    fun build(): List<Node> = nodes.toList()
}

fun fragment(
    content: FragmentBuilder.() -> Unit,
): List<Node> = FragmentBuilder().apply(content).build()

fun NodeContainerBuilder.renderIf(
    condition: Boolean,
    content: NodeContainerBuilder.() -> Unit,
) {
    if (condition) {
        content()
    }
}

fun NodeContainerBuilder.renderUnless(
    condition: Boolean,
    content: NodeContainerBuilder.() -> Unit,
) {
    if (!condition) {
        content()
    }
}

fun NodeContainerBuilder.repeat(
    count: Int,
    content: NodeContainerBuilder.(index: Int) -> Unit,
) {
    kotlin.repeat(count) { index ->
        content(index)
    }
}

fun <T> NodeContainerBuilder.renderEach(
    items: Iterable<T>,
    content: NodeContainerBuilder.(item: T) -> Unit,
) {
    items.forEach { item ->
        content(item)
    }
}

fun <T> NodeContainerBuilder.renderState(
    state: RenderState<T>,
    loading: FragmentBuilder.() -> Unit = {
        label("Loading...")
    },
    empty: FragmentBuilder.() -> Unit = {},
    failed: FragmentBuilder.(RenderState.Failed) -> Unit = { failure ->
        label(failure.message)
    },
    content: FragmentBuilder.(T) -> Unit,
) {
    when (state) {
        RenderState.Loading -> fragment(loading).forEach(::add)
        RenderState.Empty -> fragment(empty).forEach(::add)
        is RenderState.Failed -> fragment { failed(state) }.forEach(::add)
        is RenderState.Content -> fragment { content(state.value) }.forEach(::add)
    }
}

fun <T> Pane.bindContent(
    scope: CoroutineScope,
    flow: Flow<T>,
    content: FragmentBuilder.(value: T) -> Unit,
): Job = flow.collectLatestIn(scope) { value ->
    replaceChildren {
        content(value)
    }
}

fun <T> Pane.bindEach(
    scope: CoroutineScope,
    flow: Flow<List<T>>,
    content: FragmentBuilder.(item: T) -> Unit,
): Job = bindList(
    scope = scope,
    flow = flow,
    item = content,
)

fun <T> Pane.bindList(
    scope: CoroutineScope,
    flow: Flow<List<T>>,
    empty: FragmentBuilder.() -> Unit = {},
    item: FragmentBuilder.(item: T) -> Unit,
): Job = flow.collectLatestIn(scope) { items ->
    replaceChildren {
        if (items.isEmpty()) {
            empty()
        } else {
            items.forEach { value ->
                item(value)
            }
        }
    }
}

fun <T> Pane.bindRenderState(
    scope: CoroutineScope,
    flow: Flow<RenderState<T>>,
    loading: FragmentBuilder.() -> Unit = {
        label("Loading...")
    },
    empty: FragmentBuilder.() -> Unit = {},
    failed: FragmentBuilder.(RenderState.Failed) -> Unit = { failure ->
        label(failure.message)
    },
    content: FragmentBuilder.(value: T) -> Unit,
): Job = flow.collectLatestIn(scope) { state ->
    replaceChildren {
        renderState(
            state = state,
            loading = loading,
            empty = empty,
            failed = failed,
            content = content,
        )
    }
}
