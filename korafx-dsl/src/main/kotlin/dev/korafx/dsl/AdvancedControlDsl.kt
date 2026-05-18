package dev.korafx.dsl

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.scene.Node
import javafx.scene.control.Accordion
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.Pagination
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.SelectionMode
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.TitledPane
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.MouseButton
import javafx.scene.layout.StackPane

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

class AccordionBuilder internal constructor(
    private val accordion: Accordion,
) {
    fun pane(
        title: String,
        expanded: Boolean = true,
        init: TitledPane.() -> Unit = {},
        content: () -> Node,
    ): TitledPane =
        titledPane(title, expanded, init, content).also {
            accordion.panes += it
        }
}

class TreeViewBuilder<T> internal constructor(
    private val treeView: TreeView<T>,
) {
    private var textRenderer: ((T) -> String)? = null
    private var nodeRenderer: (CellContentBuilder.(T) -> Unit)? = null
    private var rowClickCount: Int = 2
    private var rowMouseButton: MouseButton = MouseButton.PRIMARY
    private var rowActionHandler: ((T) -> Unit)? = null

    fun root(
        value: T,
        expanded: Boolean = true,
        content: TreeItemBuilder<T>.() -> Unit = {},
    ): TreeItem<T> =
        treeItem(value, expanded, content).also {
            treeView.root = it
        }

    fun showRoot(show: Boolean) {
        treeView.isShowRoot = show
    }

    fun render(textOf: (T) -> String) {
        textRenderer = textOf
        nodeRenderer = null
        installCellFactory()
    }

    fun cell(content: CellContentBuilder.(T) -> Unit) {
        nodeRenderer = content
        textRenderer = null
        installCellFactory()
    }

    fun rowAction(
        clickCount: Int = 2,
        mouseButton: MouseButton = MouseButton.PRIMARY,
        handler: (T) -> Unit,
    ) {
        rowClickCount = clickCount
        rowMouseButton = mouseButton
        rowActionHandler = handler
        installCellFactory()
    }

    fun onSelect(handler: (T?) -> Unit) {
        treeView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            handler(newValue?.value)
        }
    }

    private fun installCellFactory() {
        treeView.setCellFactory {
            object : TreeCell<T>() {
                init {
                    setOnMouseClicked { event ->
                        if (
                            event.button == rowMouseButton &&
                            event.clickCount == rowClickCount &&
                            item != null &&
                            !isEmpty
                        ) {
                            rowActionHandler?.invoke(item)
                        }
                    }
                }

                override fun updateItem(item: T?, empty: Boolean) {
                    super.updateItem(item, empty)

                    if (empty || item == null) {
                        text = null
                        graphic = null
                        return
                    }

                    val content = nodeRenderer
                    if (content != null) {
                        text = null
                        graphic = StackPane().apply {
                            CellContentBuilder(this).content(item)
                        }
                        return
                    }

                    text = textRenderer?.invoke(item) ?: item.toString()
                    graphic = treeItem?.graphic
                }
            }
        }
    }
}

class TreeItemBuilder<T> internal constructor(
    private val treeItem: TreeItem<T>,
) {
    fun item(
        value: T,
        expanded: Boolean = true,
        content: TreeItemBuilder<T>.() -> Unit = {},
    ): TreeItem<T> =
        treeItem(value, expanded, content).also {
            treeItem.children += it
        }
}

class TableViewBuilder<T> internal constructor(
    private val tableView: TableView<T>,
) {
    fun items(items: Iterable<T>) {
        tableView.items.setAll(items.toList())
    }

    fun selectionMode(mode: SelectionMode) {
        tableView.selectionModel.selectionMode = mode
    }

    fun placeholder(text: String, init: Label.() -> Unit = {}) {
        tableView.placeholder = Label(text).apply(init)
    }

    fun placeholder(node: Node) {
        tableView.placeholder = node
    }

    fun constrainedResize() {
        tableView.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
    }

    fun clearColumns() {
        tableView.columns.clear()
    }

    fun onSelect(handler: (T?) -> Unit) {
        tableView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            handler(newValue)
        }
    }

    fun rowAction(
        clickCount: Int = 2,
        mouseButton: MouseButton = MouseButton.PRIMARY,
        handler: (T) -> Unit,
    ) {
        tableView.setRowFactory {
            object : TableRow<T>() {
                init {
                    setOnMouseClicked { event ->
                        if (event.button == mouseButton && event.clickCount == clickCount && item != null && !isEmpty) {
                            handler(item)
                        }
                    }
                }
            }
        }
    }

    fun <R> column(
        title: String,
        valueOf: (T) -> R,
        init: TableColumn<T, R>.() -> Unit = {},
    ): TableColumn<T, R> =
        TableColumn<T, R>(title).apply {
            setCellValueFactory { features ->
                ReadOnlyObjectWrapper(valueOf(features.value))
            }
            init()
        }.also {
            tableView.columns += it
        }

    fun textColumn(
        title: String,
        valueOf: (T) -> Any?,
    ): TableColumn<T, String> =
        textColumn(title, valueOf, init = {})

    fun textColumn(
        title: String,
        valueOf: (T) -> Any?,
        init: TableColumn<T, String>.() -> Unit,
    ): TableColumn<T, String> =
        column(title, valueOf = { row -> valueOf(row)?.toString().orEmpty() }, init = init)

    fun <R> column(
        title: String,
        valueOf: (T) -> R,
        render: (R) -> String,
        init: TableColumn<T, R>.() -> Unit = {},
    ): TableColumn<T, R> =
        column(title, valueOf) {
            setCellFactory {
                object : TableCell<T, R>() {
                    override fun updateItem(item: R?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (empty || item == null) null else render(item)
                    }
                }
            }
            init()
        }

    fun <R> columnNode(
        title: String,
        valueOf: (T) -> R,
        init: TableColumn<T, R>.() -> Unit = {},
        content: CellContentBuilder.(R) -> Unit,
    ): TableColumn<T, R> =
        column(title, valueOf) {
            setCellFactory {
                object : TableCell<T, R>() {
                    override fun updateItem(item: R?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = null
                        graphic =
                            if (empty || item == null) {
                                null
                            } else {
                                StackPane().apply {
                                    CellContentBuilder(this).content(item)
                                }
                            }
                    }
                }
            }
            init()
        }

    fun actionColumn(
        title: String = "",
        text: String,
        init: Button.() -> Unit = {},
        handler: (T) -> Unit,
    ): TableColumn<T, T> =
        columnNode(
            title = title,
            valueOf = { row -> row },
        ) { row ->
            button(text) {
                init()
                onAction {
                    handler(row)
                }
            }
        }
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
