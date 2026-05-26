@file:JvmName("AdvancedControlDslKt")
@file:JvmMultifileClass

package dev.korafx.dsl

import javafx.scene.Node
import javafx.scene.control.Accordion
import javafx.scene.control.Hyperlink
import javafx.scene.control.Pagination
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.TableView
import javafx.scene.control.TitledPane
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView

fun accordion(
    init: Accordion.() -> Unit = {},
    content: AccordionBuilder.() -> Unit,
): Accordion =
    Accordion().apply(init).apply {
        AccordionBuilder(this).content()
    }

fun titledPane(
    title: String,
    expanded: Boolean = true,
    init: TitledPane.() -> Unit = {},
    content: () -> Node,
): TitledPane =
    TitledPane(title, content()).apply {
        isExpanded = expanded
        init()
    }

fun progressBar(
    progress: Double = ProgressIndicator.INDETERMINATE_PROGRESS,
    init: ProgressBar.() -> Unit = {},
): ProgressBar =
    ProgressBar(progress).apply(init)

fun progressIndicator(
    progress: Double = ProgressIndicator.INDETERMINATE_PROGRESS,
    init: ProgressIndicator.() -> Unit = {},
): ProgressIndicator =
    ProgressIndicator(progress).apply(init)

fun <T> spinner(
    valueFactory: SpinnerValueFactory<T>,
    init: Spinner<T>.() -> Unit = {},
): Spinner<T> =
    Spinner<T>().apply {
        this.valueFactory = valueFactory
        init()
    }

fun intSpinner(
    min: Int,
    max: Int,
    initialValue: Int = min,
    amountToStepBy: Int = 1,
    init: Spinner<Int>.() -> Unit = {},
): Spinner<Int> =
    spinner(
        SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue, amountToStepBy),
        init,
    )

fun doubleSpinner(
    min: Double,
    max: Double,
    initialValue: Double = min,
    amountToStepBy: Double = 1.0,
    init: Spinner<Double>.() -> Unit = {},
): Spinner<Double> =
    spinner(
        SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initialValue, amountToStepBy),
        init,
    )

fun hyperlink(
    text: String,
    init: Hyperlink.() -> Unit = {},
): Hyperlink =
    Hyperlink(text).apply(init)

fun pagination(
    pageCount: Int,
    init: Pagination.() -> Unit = {},
    pageFactory: (pageIndex: Int) -> Node,
): Pagination =
    Pagination(pageCount).apply {
        this.pageFactory = javafx.util.Callback(pageFactory)
        init()
    }

fun <T> treeView(
    root: TreeItem<T>? = null,
    init: TreeView<T>.() -> Unit = {},
    content: TreeViewBuilder<T>.() -> Unit = {},
): TreeView<T> =
    TreeView<T>().apply {
        this.root = root
        init()
        TreeViewBuilder(this).content()
    }

fun <T> treeItem(
    value: T,
    expanded: Boolean = true,
    content: TreeItemBuilder<T>.() -> Unit = {},
): TreeItem<T> =
    TreeItem<T>(value).apply {
        isExpanded = expanded
        TreeItemBuilder(this).content()
    }

fun <T> tableView(
    items: Iterable<T> = emptyList(),
    init: TableView<T>.() -> Unit = {},
    content: TableViewBuilder<T>.() -> Unit = {},
): TableView<T> =
    TableView<T>().apply {
        this.items.setAll(items.toList())
        init()
        TableViewBuilder(this).content()
    }

fun NodeContainerBuilder.accordion(
    init: Accordion.() -> Unit = {},
    content: AccordionBuilder.() -> Unit,
): Accordion = add(dev.korafx.dsl.accordion(init, content))

fun NodeContainerBuilder.titledPane(
    title: String,
    expanded: Boolean = true,
    init: TitledPane.() -> Unit = {},
    content: () -> Node,
): TitledPane = add(dev.korafx.dsl.titledPane(title, expanded, init, content))

fun NodeContainerBuilder.progressBar(
    progress: Double = ProgressIndicator.INDETERMINATE_PROGRESS,
    init: ProgressBar.() -> Unit = {},
): ProgressBar = add(dev.korafx.dsl.progressBar(progress, init))

fun NodeContainerBuilder.progressIndicator(
    progress: Double = ProgressIndicator.INDETERMINATE_PROGRESS,
    init: ProgressIndicator.() -> Unit = {},
): ProgressIndicator = add(dev.korafx.dsl.progressIndicator(progress, init))

fun <T> NodeContainerBuilder.spinner(
    valueFactory: SpinnerValueFactory<T>,
    init: Spinner<T>.() -> Unit = {},
): Spinner<T> = add(dev.korafx.dsl.spinner(valueFactory, init))

fun NodeContainerBuilder.intSpinner(
    min: Int,
    max: Int,
    initialValue: Int = min,
    amountToStepBy: Int = 1,
    init: Spinner<Int>.() -> Unit = {},
): Spinner<Int> = add(dev.korafx.dsl.intSpinner(min, max, initialValue, amountToStepBy, init))

fun NodeContainerBuilder.doubleSpinner(
    min: Double,
    max: Double,
    initialValue: Double = min,
    amountToStepBy: Double = 1.0,
    init: Spinner<Double>.() -> Unit = {},
): Spinner<Double> = add(dev.korafx.dsl.doubleSpinner(min, max, initialValue, amountToStepBy, init))

fun NodeContainerBuilder.hyperlink(
    text: String,
    init: Hyperlink.() -> Unit = {},
): Hyperlink = add(dev.korafx.dsl.hyperlink(text, init))

fun NodeContainerBuilder.pagination(
    pageCount: Int,
    init: Pagination.() -> Unit = {},
    pageFactory: (pageIndex: Int) -> Node,
): Pagination = add(dev.korafx.dsl.pagination(pageCount, init, pageFactory))

fun <T> NodeContainerBuilder.treeView(
    root: TreeItem<T>? = null,
    init: TreeView<T>.() -> Unit = {},
    content: TreeViewBuilder<T>.() -> Unit = {},
): TreeView<T> = add(dev.korafx.dsl.treeView(root, init, content))

fun <T> NodeContainerBuilder.tableView(
    items: Iterable<T> = emptyList(),
    init: TableView<T>.() -> Unit = {},
    content: TableViewBuilder<T>.() -> Unit = {},
): TableView<T> = add(dev.korafx.dsl.tableView(items, init, content))
