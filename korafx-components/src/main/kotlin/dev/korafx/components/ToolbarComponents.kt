package dev.korafx.components

import dev.korafx.dsl.HBoxBuilder
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.label
import dev.korafx.dsl.styleClass
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import org.kordamp.ikonli.Ikon

fun appToolbar(
    title: String,
    subtitle: String? = null,
    icon: Ikon? = null,
    iconSize: Int = 18,
    spacing: Double = 12.0,
    init: HBox.() -> Unit = {},
    navigation: HBoxBuilder.() -> Unit = {},
    content: HBoxBuilder.() -> Unit = {},
    actions: HBoxBuilder.() -> Unit = {},
): HBox =
    dev.korafx.dsl.hbox(
        spacing = spacing,
        init = {
            styleClass("app-toolbar")
            alignment = Pos.CENTER_LEFT
            init()
        },
    ) {
        add(
            dev.korafx.components.toolbarGroup(
                styleClass = "app-toolbar-navigation",
                content = navigation,
            ),
        )
        add(
            dev.korafx.dsl.vbox(
                spacing = 2.0,
                init = {
                    styleClass("app-toolbar-title-stack")
                },
            ) {
                label(title) {
                    styleClass("app-toolbar-title")
                    if (icon != null) {
                        setKoraIcon(icon, iconSize)
                    }
                }
                if (subtitle != null) {
                    label(subtitle) {
                        styleClass("app-toolbar-subtitle")
                    }
                }
            },
        )
        add(
            dev.korafx.components.toolbarGroup(
                styleClass = "app-toolbar-content",
                content = content,
            ),
        )
        add(Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        })
        add(
            dev.korafx.components.toolbarGroup(
                styleClass = "app-toolbar-actions",
                content = actions,
            ),
        )
    }

fun toolbarGroup(
    spacing: Double = 8.0,
    styleClass: String = "toolbar-group",
    init: HBox.() -> Unit = {},
    content: HBoxBuilder.() -> Unit = {},
): HBox =
    dev.korafx.dsl.hbox(
        spacing = spacing,
        init = {
            styleClass("toolbar-group")
            styleClass(styleClass)
            alignment = Pos.CENTER_LEFT
            init()
        },
        content = content,
    )

fun NodeContainerBuilder.appToolbar(
    title: String,
    subtitle: String? = null,
    icon: Ikon? = null,
    iconSize: Int = 18,
    spacing: Double = 12.0,
    init: HBox.() -> Unit = {},
    navigation: HBoxBuilder.() -> Unit = {},
    content: HBoxBuilder.() -> Unit = {},
    actions: HBoxBuilder.() -> Unit = {},
): HBox =
    add(
        dev.korafx.components.appToolbar(
            title = title,
            subtitle = subtitle,
            icon = icon,
            iconSize = iconSize,
            spacing = spacing,
            init = init,
            navigation = navigation,
            content = content,
            actions = actions,
        ),
    )

fun NodeContainerBuilder.toolbarGroup(
    spacing: Double = 8.0,
    styleClass: String = "toolbar-group",
    init: HBox.() -> Unit = {},
    content: HBoxBuilder.() -> Unit = {},
): HBox =
    add(
        dev.korafx.components.toolbarGroup(
            spacing = spacing,
            styleClass = styleClass,
            init = init,
            content = content,
        ),
    )
