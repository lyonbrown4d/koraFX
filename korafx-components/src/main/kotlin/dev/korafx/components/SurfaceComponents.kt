package dev.korafx.components

import dev.korafx.dsl.HBoxBuilder
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.VBoxBuilder
import dev.korafx.dsl.hbox
import dev.korafx.dsl.label
import dev.korafx.dsl.paddingAll
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.vbox
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.kordamp.ikonli.Ikon

fun card(
    spacing: Double = 12.0,
    padding: Double = 18.0,
    init: VBox.() -> Unit = {},
    content: VBoxBuilder.() -> Unit,
): VBox =
    vbox(
        spacing = spacing,
        init = {
            styleClass("card")
            paddingAll(padding)
            init()
        },
        content = content,
    )

fun section(
    title: String,
    description: String? = null,
    spacing: Double = 14.0,
    padding: Double = 18.0,
    init: VBox.() -> Unit = {},
    content: VBoxBuilder.() -> Unit = {},
): VBox =
    card(
        spacing = spacing,
        padding = padding,
        init = {
            styleClass("section")
            init()
        },
    ) {
        label(title) {
            styleClasses("section-title", "headline")
            isWrapText = true
        }

        if (description != null) {
            label(description) {
                styleClasses("section-description", "muted")
                isWrapText = true
            }
        }

        content()
    }

fun actionBar(
    spacing: Double = 10.0,
    alignEnd: Boolean = true,
    init: HBox.() -> Unit = {},
    content: HBoxBuilder.() -> Unit,
): HBox =
    hbox(
        spacing = spacing,
        init = {
            styleClass("action-bar")
            alignment = Pos.CENTER_LEFT
            init()
        },
    ) {
        if (alignEnd) {
            spacer()
        }
        content()
    }

fun statusBar(
    spacing: Double = 10.0,
    init: HBox.() -> Unit = {},
    content: HBoxBuilder.() -> Unit,
): HBox =
    hbox(
        spacing = spacing,
        init = {
            styleClass("status-bar")
            alignment = Pos.CENTER_LEFT
            init()
        },
        content = content,
    )

fun statusItem(
    text: String,
    tone: ComponentTone = ComponentTone.NEUTRAL,
    icon: Ikon? = null,
    iconSize: Int = 13,
    init: Label.() -> Unit = {},
): Label =
    Label(text).apply {
        styleClasses("status-item", tone.styleClass)
        if (icon != null) {
            setKoraIcon(icon, iconSize)
        }
        init()
    }

fun NodeContainerBuilder.card(
    spacing: Double = 12.0,
    padding: Double = 18.0,
    init: VBox.() -> Unit = {},
    content: VBoxBuilder.() -> Unit,
): VBox =
    add(
        dev.korafx.components.card(
            spacing = spacing,
            padding = padding,
            init = init,
            content = content,
        ),
    )

fun NodeContainerBuilder.section(
    title: String,
    description: String? = null,
    spacing: Double = 14.0,
    padding: Double = 18.0,
    init: VBox.() -> Unit = {},
    content: VBoxBuilder.() -> Unit = {},
): VBox =
    add(
        dev.korafx.components.section(
            title = title,
            description = description,
            spacing = spacing,
            padding = padding,
            init = init,
            content = content,
        ),
    )

fun NodeContainerBuilder.actionBar(
    spacing: Double = 10.0,
    alignEnd: Boolean = true,
    init: HBox.() -> Unit = {},
    content: HBoxBuilder.() -> Unit,
): HBox =
    add(
        dev.korafx.components.actionBar(
            spacing = spacing,
            alignEnd = alignEnd,
            init = init,
            content = content,
        ),
    )

fun NodeContainerBuilder.statusBar(
    spacing: Double = 10.0,
    init: HBox.() -> Unit = {},
    content: HBoxBuilder.() -> Unit,
): HBox =
    add(
        dev.korafx.components.statusBar(
            spacing = spacing,
            init = init,
            content = content,
        ),
    )

fun NodeContainerBuilder.statusItem(
    text: String,
    tone: ComponentTone = ComponentTone.NEUTRAL,
    icon: Ikon? = null,
    iconSize: Int = 13,
    init: Label.() -> Unit = {},
): Label =
    add(
        dev.korafx.components.statusItem(
            text = text,
            tone = tone,
            icon = icon,
            iconSize = iconSize,
            init = init,
        ),
    )
