@file:JvmName("LayoutDslKt")
@file:JvmMultifileClass

package dev.korafx.dsl

import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.MenuBar
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.control.SplitPane
import javafx.scene.control.TabPane
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.ToolBar
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
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
