@file:JvmName("StatefulDslKt")
@file:JvmMultifileClass

package dev.korafx.dsl

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Labeled
import javafx.scene.control.TextInputControl
import javafx.scene.layout.Pane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

fun <S, N : Node> stateful(
    scope: CoroutineScope,
    state: Flow<S>,
    content: StatefulRootBuilder<S>.() -> N,
): N =
    StatefulRootBuilder(scope, state).content()

fun <S, N : Node> NodeContainerBuilder.stateful(
    scope: CoroutineScope,
    state: Flow<S>,
    content: StatefulRootBuilder<S>.() -> N,
): N =
    add(dev.korafx.dsl.stateful(scope, state, content))

fun <S, N : Node> N.stateVisible(
    scope: CoroutineScope,
    state: Flow<S>,
    manageWhenHidden: Boolean = true,
    visible: (S) -> Boolean,
): N =
    also {
        bindVisible(scope, state.map(visible).distinctUntilChanged(), manageWhenHidden)
    }

fun <S, N : Node> N.stateDisable(
    scope: CoroutineScope,
    state: Flow<S>,
    disabled: (S) -> Boolean,
): N =
    also {
        bindDisable(scope, state.map(disabled).distinctUntilChanged())
    }

fun <S, N : Node> N.stateStyle(
    scope: CoroutineScope,
    state: Flow<S>,
    style: (S) -> CssStyle?,
): N =
    also {
        bindStyle(scope, state.map(style).distinctUntilChanged())
    }

fun <S, N : Node> N.stateStyleClass(
    scope: CoroutineScope,
    state: Flow<S>,
    className: String,
    enabled: (S) -> Boolean,
): N =
    also {
        bindStyleClass(scope, className, state.map(enabled).distinctUntilChanged())
    }

fun <S, L : Labeled> L.stateText(
    scope: CoroutineScope,
    state: Flow<S>,
    text: (S) -> Any?,
): L =
    also {
        bindText(scope, state.map { value -> text(value)?.toString().orEmpty() }.distinctUntilChanged())
    }

fun <S, T : TextInputControl> T.stateText(
    scope: CoroutineScope,
    state: Flow<S>,
    onTextChange: ((String) -> Unit)? = null,
    text: (S) -> String,
): T =
    also {
        bindTextToState(scope, state.map(text).distinctUntilChanged(), onTextChange)
    }

fun <S, L : Label> L.stateValidation(
    scope: CoroutineScope,
    state: Flow<S>,
    message: (S) -> String?,
): L =
    also {
        bindValidation(scope, state.map(message).distinctUntilChanged())
    }

fun <S, T, P : Pane> P.stateList(
    scope: CoroutineScope,
    state: Flow<S>,
    items: (S) -> List<T>,
    empty: FragmentBuilder.() -> Unit = {},
    item: FragmentBuilder.(T) -> Unit,
): P =
    also {
        bindList(
            scope = scope,
            flow = state.map(items).distinctUntilChanged(),
            empty = empty,
            item = item,
        )
    }
