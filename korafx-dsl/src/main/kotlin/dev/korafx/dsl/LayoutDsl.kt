package dev.korafx.dsl

import javafx.geometry.Insets
import javafx.geometry.HPos
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ButtonBase
import javafx.scene.control.CheckBox
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.Separator
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.ToolBar
import javafx.css.PseudoClass
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.FlowPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.RowConstraints
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox

fun label(
    text: String = "",
    init: Label.() -> Unit = {},
): Label = Label(text).apply(init)

fun button(
    text: String,
    init: Button.() -> Unit = {},
): Button = Button(text).apply(init)

fun checkBox(
    text: String,
    init: CheckBox.() -> Unit = {},
): CheckBox = CheckBox(text).apply(init)

fun textField(
    text: String = "",
    init: TextField.() -> Unit = {},
): TextField = TextField(text).apply(init)

fun textArea(
    text: String = "",
    init: TextArea.() -> Unit = {},
): TextArea = TextArea(text).apply(init)

fun separator(
    init: Separator.() -> Unit = {},
): Separator = Separator().apply(init)

fun region(
    init: Region.() -> Unit = {},
): Region = Region().apply(init)

fun borderPane(
    init: BorderPane.() -> Unit = {},
    content: BorderPaneBuilder.() -> Unit,
): BorderPane =
    BorderPane().apply(init).apply {
        BorderPaneBuilder(this).content()
    }

fun vbox(
    spacing: Double = 0.0,
    init: VBox.() -> Unit = {},
    content: VBoxBuilder.() -> Unit,
): VBox =
    VBox(spacing).apply(init).apply {
        VBoxBuilder(this).content()
    }

fun hbox(
    spacing: Double = 0.0,
    init: HBox.() -> Unit = {},
    content: HBoxBuilder.() -> Unit,
): HBox =
    HBox(spacing).apply(init).apply {
        HBoxBuilder(this).content()
    }

fun stackPane(
    init: StackPane.() -> Unit = {},
    content: StackPaneBuilder.() -> Unit,
): StackPane =
    StackPane().apply(init).apply {
        StackPaneBuilder(this).content()
    }

fun flowPane(
    hgap: Double = 0.0,
    vgap: Double = 0.0,
    init: FlowPane.() -> Unit = {},
    content: FlowPaneBuilder.() -> Unit,
): FlowPane =
    FlowPane(hgap, vgap).apply(init).apply {
        FlowPaneBuilder(this).content()
    }

fun tilePane(
    hgap: Double = 0.0,
    vgap: Double = 0.0,
    init: TilePane.() -> Unit = {},
    content: TilePaneBuilder.() -> Unit,
): TilePane =
    TilePane(hgap, vgap).apply(init).apply {
        TilePaneBuilder(this).content()
    }

fun anchorPane(
    init: AnchorPane.() -> Unit = {},
    content: AnchorPaneBuilder.() -> Unit,
): AnchorPane =
    AnchorPane().apply(init).apply {
        AnchorPaneBuilder(this).content()
    }

fun gridPane(
    hgap: Double = 0.0,
    vgap: Double = 0.0,
    init: GridPane.() -> Unit = {},
    content: GridPaneBuilder.() -> Unit,
): GridPane =
    GridPane().apply {
        this.hgap = hgap
        this.vgap = vgap
    }.apply(init).apply {
        GridPaneBuilder(this).content()
    }

fun toolbar(
    init: ToolBar.() -> Unit = {},
    content: ToolBarBuilder.() -> Unit,
): ToolBar =
    ToolBar().apply(init).apply {
        ToolBarBuilder(this).content()
    }

fun scrollPane(
    init: ScrollPane.() -> Unit = {},
    content: ScrollPaneBuilder.() -> Unit,
): ScrollPane =
    ScrollPane().apply(init).apply {
        ScrollPaneBuilder(this).content()
    }

fun splitPane(
    orientation: Orientation = Orientation.HORIZONTAL,
    init: SplitPane.() -> Unit = {},
    content: SplitPaneBuilder.() -> Unit,
): SplitPane =
    SplitPane().apply {
        this.orientation = orientation
    }.apply(init).apply {
        SplitPaneBuilder(this).content()
    }

fun tabPane(
    init: TabPane.() -> Unit = {},
    content: TabPaneBuilder.() -> Unit,
): TabPane =
    TabPane().apply(init).apply {
        TabPaneBuilder(this).content()
    }

fun menuBar(
    init: MenuBar.() -> Unit = {},
    content: MenuBarBuilder.() -> Unit,
): MenuBar =
    MenuBar().apply(init).apply {
        MenuBarBuilder(this).content()
    }

abstract class NodeContainerBuilder internal constructor() {
    protected abstract fun append(node: Node)

    fun <T : Node> add(node: T): T {
        append(node)
        return node
    }

    fun label(text: String = "", init: Label.() -> Unit = {}): Label =
        add(dev.korafx.dsl.label(text, init))

    fun button(text: String, init: Button.() -> Unit = {}): Button =
        add(dev.korafx.dsl.button(text, init))

    fun checkBox(text: String, init: CheckBox.() -> Unit = {}): CheckBox =
        add(dev.korafx.dsl.checkBox(text, init))

    fun textField(text: String = "", init: TextField.() -> Unit = {}): TextField =
        add(dev.korafx.dsl.textField(text, init))

    fun textArea(text: String = "", init: TextArea.() -> Unit = {}): TextArea =
        add(dev.korafx.dsl.textArea(text, init))

    fun separator(init: Separator.() -> Unit = {}): Separator =
        add(dev.korafx.dsl.separator(init))

    fun region(init: Region.() -> Unit = {}): Region =
        add(dev.korafx.dsl.region(init))

    fun vbox(
        spacing: Double = 0.0,
        init: VBox.() -> Unit = {},
        content: VBoxBuilder.() -> Unit,
    ): VBox = add(dev.korafx.dsl.vbox(spacing, init, content))

    fun hbox(
        spacing: Double = 0.0,
        init: HBox.() -> Unit = {},
        content: HBoxBuilder.() -> Unit,
    ): HBox = add(dev.korafx.dsl.hbox(spacing, init, content))

    fun stackPane(
        init: StackPane.() -> Unit = {},
        content: StackPaneBuilder.() -> Unit,
    ): StackPane = add(dev.korafx.dsl.stackPane(init, content))

    fun flowPane(
        hgap: Double = 0.0,
        vgap: Double = 0.0,
        init: FlowPane.() -> Unit = {},
        content: FlowPaneBuilder.() -> Unit,
    ): FlowPane = add(dev.korafx.dsl.flowPane(hgap, vgap, init, content))

    fun tilePane(
        hgap: Double = 0.0,
        vgap: Double = 0.0,
        init: TilePane.() -> Unit = {},
        content: TilePaneBuilder.() -> Unit,
    ): TilePane = add(dev.korafx.dsl.tilePane(hgap, vgap, init, content))

    fun anchorPane(
        init: AnchorPane.() -> Unit = {},
        content: AnchorPaneBuilder.() -> Unit,
    ): AnchorPane = add(dev.korafx.dsl.anchorPane(init, content))

    fun gridPane(
        hgap: Double = 0.0,
        vgap: Double = 0.0,
        init: GridPane.() -> Unit = {},
        content: GridPaneBuilder.() -> Unit,
    ): GridPane = add(dev.korafx.dsl.gridPane(hgap, vgap, init, content))

    fun toolbar(
        init: ToolBar.() -> Unit = {},
        content: ToolBarBuilder.() -> Unit,
    ): ToolBar = add(dev.korafx.dsl.toolbar(init, content))

    fun scrollPane(
        init: ScrollPane.() -> Unit = {},
        content: ScrollPaneBuilder.() -> Unit,
    ): ScrollPane = add(dev.korafx.dsl.scrollPane(init, content))

    fun splitPane(
        orientation: Orientation = Orientation.HORIZONTAL,
        init: SplitPane.() -> Unit = {},
        content: SplitPaneBuilder.() -> Unit,
    ): SplitPane = add(dev.korafx.dsl.splitPane(orientation, init, content))

    fun tabPane(
        init: TabPane.() -> Unit = {},
        content: TabPaneBuilder.() -> Unit,
    ): TabPane = add(dev.korafx.dsl.tabPane(init, content))

    fun spacer(
        minWidth: Double = 0.0,
        minHeight: Double = 0.0,
        grow: Priority = Priority.ALWAYS,
    ): Region =
        region {
            this.minWidth = minWidth
            this.minHeight = minHeight
            HBox.setHgrow(this, grow)
            VBox.setVgrow(this, grow)
        }
}

abstract class PaneBuilder<T : Pane> internal constructor(
    protected val pane: T,
) : NodeContainerBuilder() {
    override fun append(node: Node) {
        pane.children += node
    }
}

class BorderPaneBuilder internal constructor(
    private val pane: BorderPane,
) {
    fun top(node: Node) {
        pane.top = node
    }

    fun top(factory: () -> Node) {
        top(factory())
    }

    fun right(node: Node) {
        pane.right = node
    }

    fun right(factory: () -> Node) {
        right(factory())
    }

    fun bottom(node: Node) {
        pane.bottom = node
    }

    fun bottom(factory: () -> Node) {
        bottom(factory())
    }

    fun left(node: Node) {
        pane.left = node
    }

    fun left(factory: () -> Node) {
        left(factory())
    }

    fun center(node: Node) {
        pane.center = node
    }

    fun center(factory: () -> Node) {
        center(factory())
    }
}

class VBoxBuilder internal constructor(
    box: VBox,
) : PaneBuilder<VBox>(box) {
    fun alignment(value: Pos) {
        pane.alignment = value
    }

    fun fillWidth(value: Boolean = true) {
        pane.isFillWidth = value
    }

    fun spacing(value: Double) {
        pane.spacing = value
    }
}

class HBoxBuilder internal constructor(
    box: HBox,
) : PaneBuilder<HBox>(box) {
    fun alignment(value: Pos) {
        pane.alignment = value
    }

    fun fillHeight(value: Boolean = true) {
        pane.isFillHeight = value
    }

    fun spacing(value: Double) {
        pane.spacing = value
    }
}

class StackPaneBuilder internal constructor(
    pane: StackPane,
) : PaneBuilder<StackPane>(pane) {
    fun alignment(value: Pos) {
        pane.alignment = value
    }
}

class FlowPaneBuilder internal constructor(
    pane: FlowPane,
) : PaneBuilder<FlowPane>(pane)

class TilePaneBuilder internal constructor(
    pane: TilePane,
) : PaneBuilder<TilePane>(pane)

class AnchorPaneBuilder internal constructor(
    private val pane: AnchorPane,
) : NodeContainerBuilder() {
    override fun append(node: Node) {
        pane.children += node
    }

    fun anchor(
        node: Node,
        top: Double? = null,
        right: Double? = null,
        bottom: Double? = null,
        left: Double? = null,
    ): Node =
        add(node).also {
            AnchorPane.setTopAnchor(it, top)
            AnchorPane.setRightAnchor(it, right)
            AnchorPane.setBottomAnchor(it, bottom)
            AnchorPane.setLeftAnchor(it, left)
        }

    fun anchor(
        top: Double? = null,
        right: Double? = null,
        bottom: Double? = null,
        left: Double? = null,
        factory: () -> Node,
    ): Node = anchor(factory(), top, right, bottom, left)
}

class GridPaneBuilder internal constructor(
    private val pane: GridPane,
) {
    fun column(
        minWidth: Double? = null,
        prefWidth: Double? = null,
        maxWidth: Double? = null,
        percentWidth: Double? = null,
        grow: Priority? = null,
        fillWidth: Boolean? = null,
        alignment: HPos? = null,
    ): ColumnConstraints =
        ColumnConstraints().apply {
            minWidth?.let { this.minWidth = it }
            prefWidth?.let { this.prefWidth = it }
            maxWidth?.let { this.maxWidth = it }
            percentWidth?.let { this.percentWidth = it }
            grow?.let { hgrow = it }
            fillWidth?.let { isFillWidth = it }
            alignment?.let { halignment = it }
        }.also {
            pane.columnConstraints += it
        }

    fun columns(
        count: Int,
        minWidth: Double? = null,
        prefWidth: Double? = null,
        maxWidth: Double? = null,
        percentWidth: Double? = null,
        grow: Priority? = null,
        fillWidth: Boolean? = null,
        alignment: HPos? = null,
    ): List<ColumnConstraints> {
        require(count >= 0) {
            "GridPane column count must be non-negative."
        }

        return List(count) {
            column(
                minWidth = minWidth,
                prefWidth = prefWidth,
                maxWidth = maxWidth,
                percentWidth = percentWidth,
                grow = grow,
                fillWidth = fillWidth,
                alignment = alignment,
            )
        }
    }

    fun row(
        minHeight: Double? = null,
        prefHeight: Double? = null,
        maxHeight: Double? = null,
        percentHeight: Double? = null,
        grow: Priority? = null,
        fillHeight: Boolean? = null,
        alignment: VPos? = null,
    ): RowConstraints =
        RowConstraints().apply {
            minHeight?.let { this.minHeight = it }
            prefHeight?.let { this.prefHeight = it }
            maxHeight?.let { this.maxHeight = it }
            percentHeight?.let { this.percentHeight = it }
            grow?.let { vgrow = it }
            fillHeight?.let { isFillHeight = it }
            alignment?.let { valignment = it }
        }.also {
            pane.rowConstraints += it
        }

    fun rows(
        count: Int,
        minHeight: Double? = null,
        prefHeight: Double? = null,
        maxHeight: Double? = null,
        percentHeight: Double? = null,
        grow: Priority? = null,
        fillHeight: Boolean? = null,
        alignment: VPos? = null,
    ): List<RowConstraints> {
        require(count >= 0) {
            "GridPane row count must be non-negative."
        }

        return List(count) {
            row(
                minHeight = minHeight,
                prefHeight = prefHeight,
                maxHeight = maxHeight,
                percentHeight = percentHeight,
                grow = grow,
                fillHeight = fillHeight,
                alignment = alignment,
            )
        }
    }

    fun <T : Node> cell(
        column: Int,
        row: Int,
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        node: T,
        horizontalAlignment: HPos? = null,
        verticalAlignment: VPos? = null,
        horizontalGrow: Priority? = null,
        verticalGrow: Priority? = null,
        margin: Insets? = null,
    ): T =
        node.also {
            pane.add(it, column, row, columnSpan, rowSpan)
            horizontalAlignment?.let { alignment -> GridPane.setHalignment(it, alignment) }
            verticalAlignment?.let { alignment -> GridPane.setValignment(it, alignment) }
            horizontalGrow?.let { grow -> GridPane.setHgrow(it, grow) }
            verticalGrow?.let { grow -> GridPane.setVgrow(it, grow) }
            margin?.let { value -> GridPane.setMargin(it, value) }
        }

    fun <T : Node> cell(
        column: Int,
        row: Int,
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        horizontalAlignment: HPos? = null,
        verticalAlignment: VPos? = null,
        horizontalGrow: Priority? = null,
        verticalGrow: Priority? = null,
        margin: Insets? = null,
        factory: () -> T,
    ): T =
        cell(
            column = column,
            row = row,
            columnSpan = columnSpan,
            rowSpan = rowSpan,
            node = factory(),
            horizontalAlignment = horizontalAlignment,
            verticalAlignment = verticalAlignment,
            horizontalGrow = horizontalGrow,
            verticalGrow = verticalGrow,
            margin = margin,
        )

    fun label(
        column: Int,
        row: Int,
        text: String = "",
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        init: Label.() -> Unit = {},
    ): Label =
        cell(column, row, columnSpan, rowSpan, dev.korafx.dsl.label(text, init))

    fun button(
        column: Int,
        row: Int,
        text: String,
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        init: Button.() -> Unit = {},
    ): Button =
        cell(column, row, columnSpan, rowSpan, dev.korafx.dsl.button(text, init))

    fun checkBox(
        column: Int,
        row: Int,
        text: String,
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        init: CheckBox.() -> Unit = {},
    ): CheckBox =
        cell(column, row, columnSpan, rowSpan, dev.korafx.dsl.checkBox(text, init))

    fun textField(
        column: Int,
        row: Int,
        text: String = "",
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        init: TextField.() -> Unit = {},
    ): TextField =
        cell(column, row, columnSpan, rowSpan, dev.korafx.dsl.textField(text, init))

    fun textArea(
        column: Int,
        row: Int,
        text: String = "",
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        init: TextArea.() -> Unit = {},
    ): TextArea =
        cell(column, row, columnSpan, rowSpan, dev.korafx.dsl.textArea(text, init))
}

class ToolBarBuilder internal constructor(
    private val toolBar: ToolBar,
) : NodeContainerBuilder() {
    override fun append(node: Node) {
        toolBar.items += node
    }
}

class ScrollPaneBuilder internal constructor(
    private val scrollPane: ScrollPane,
) {
    fun content(node: Node) {
        scrollPane.content = node
    }

    fun content(factory: () -> Node) {
        content(factory())
    }
}

class SplitPaneBuilder internal constructor(
    private val splitPane: SplitPane,
) : NodeContainerBuilder() {
    override fun append(node: Node) {
        splitPane.items += node
    }
}

class TabPaneBuilder internal constructor(
    private val tabPane: TabPane,
) {
    fun tab(
        title: String,
        closable: Boolean = false,
        init: Tab.() -> Unit = {},
        content: () -> Node,
    ): Tab =
        Tab(title).apply {
            isClosable = closable
            init()
            this.content = content()
        }.also {
            tabPane.tabs += it
        }
}

class MenuBarBuilder internal constructor(
    private val menuBar: MenuBar,
) {
    fun menu(
        text: String,
        init: MenuBuilder.() -> Unit,
    ): Menu =
        Menu(text).also {
            MenuBuilder(it).init()
            menuBar.menus += it
        }
}

open class MenuItemsBuilder internal constructor(
    private val items: MutableList<MenuItem>,
) {
    fun item(
        text: String,
        init: MenuItem.() -> Unit = {},
    ): MenuItem =
        MenuItem(text).apply(init).also {
            items += it
        }

    fun actionItem(
        text: String,
        action: () -> Unit,
    ): MenuItem =
        item(text) {
            onAction(action)
        }

    fun actionItem(
        text: String,
        init: MenuItem.() -> Unit,
        action: () -> Unit,
    ): MenuItem =
        item(text) {
            init()
            onAction(action)
        }

    fun checkItem(
        text: String,
        init: CheckMenuItem.() -> Unit = {},
    ): CheckMenuItem =
        CheckMenuItem(text).apply(init).also {
            items += it
        }

    fun radioItem(
        text: String,
        toggleGroup: javafx.scene.control.ToggleGroup? = null,
        init: RadioMenuItem.() -> Unit = {},
    ): RadioMenuItem =
        RadioMenuItem(text).apply {
            this.toggleGroup = toggleGroup
            init()
        }.also {
            items += it
        }

    fun submenu(
        text: String,
        init: MenuBuilder.() -> Unit,
    ): Menu =
        Menu(text).also {
            MenuBuilder(it).init()
            items += it
        }

    fun separator() {
        items += SeparatorMenuItem()
    }
}

class MenuBuilder internal constructor(
    private val menu: Menu,
) : MenuItemsBuilder(menu.items)

fun insets(value: Double): Insets =
    Insets(value)

fun insets(vertical: Double, horizontal: Double): Insets =
    Insets(vertical, horizontal, vertical, horizontal)

fun insets(
    top: Double,
    right: Double,
    bottom: Double,
    left: Double,
): Insets =
    Insets(top, right, bottom, left)

fun Region.paddingAll(value: Double) {
    padding = Insets(value)
}

fun Region.padding(vertical: Double, horizontal: Double) {
    padding = Insets(vertical, horizontal, vertical, horizontal)
}

fun Region.padding(
    top: Double,
    right: Double,
    bottom: Double,
    left: Double,
) {
    padding = Insets(top, right, bottom, left)
}

fun Region.prefSize(width: Double, height: Double) {
    prefWidth = width
    prefHeight = height
}

fun Region.minSize(width: Double, height: Double) {
    minWidth = width
    minHeight = height
}

fun Region.maxSize(width: Double, height: Double) {
    maxWidth = width
    maxHeight = height
}

fun Node.marginAll(value: Double) {
    margin(insets(value))
}

fun Node.margin(vertical: Double, horizontal: Double) {
    margin(insets(vertical, horizontal))
}

fun Node.margin(
    top: Double,
    right: Double,
    bottom: Double,
    left: Double,
) {
    margin(insets(top, right, bottom, left))
}

fun Node.margin(value: Insets) {
    HBox.setMargin(this, value)
    VBox.setMargin(this, value)
    GridPane.setMargin(this, value)
    StackPane.setMargin(this, value)
    FlowPane.setMargin(this, value)
    TilePane.setMargin(this, value)
    BorderPane.setMargin(this, value)
}

fun Node.styleClass(name: String) {
    if (!styleClass.contains(name)) {
        styleClass += name
    }
}

fun Node.styleClasses(vararg names: String) {
    names.forEach(::styleClass)
}

fun Node.removeStyleClass(name: String) {
    styleClass.remove(name)
}

fun Node.toggleStyleClass(name: String, enabled: Boolean) {
    if (enabled) {
        styleClass(name)
    } else {
        removeStyleClass(name)
    }
}

fun Node.pseudoClass(name: String, enabled: Boolean) {
    pseudoClassStateChanged(PseudoClass.getPseudoClass(name), enabled)
}

fun Node.invalidWhen(invalid: Boolean) {
    pseudoClass("invalid", invalid)
    toggleStyleClass("invalid", invalid)
}

fun Node.visibleWhen(visible: Boolean, manageWhenHidden: Boolean = true) {
    isVisible = visible
    if (manageWhenHidden) {
        isManaged = visible
    }
}

fun Node.disableWhen(disabled: Boolean) {
    isDisable = disabled
}

fun Node.growHorizontal(priority: Priority = Priority.ALWAYS) {
    HBox.setHgrow(this, priority)
    GridPane.setHgrow(this, priority)
}

fun Node.growVertical(priority: Priority = Priority.ALWAYS) {
    VBox.setVgrow(this, priority)
    GridPane.setVgrow(this, priority)
}

fun Node.align(value: Pos) {
    StackPane.setAlignment(this, value)
    BorderPane.setAlignment(this, value)
}

fun Node.gridAlign(
    horizontal: HPos? = null,
    vertical: VPos? = null,
) {
    horizontal?.let { GridPane.setHalignment(this, it) }
    vertical?.let { GridPane.setValignment(this, it) }
}

fun ButtonBase.onAction(handler: () -> Unit) {
    setOnAction { handler() }
}

fun MenuItem.onAction(handler: () -> Unit) {
    setOnAction { handler() }
}
