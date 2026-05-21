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
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.kordamp.ikonli.Ikon

fun heroBanner(
    title: String,
    subtitle: String? = null,
    eyebrow: String? = null,
    icon: Ikon? = null,
    iconSize: Int = 28,
    spacing: Double = 14.0,
    padding: Double = 22.0,
    init: VBox.() -> Unit = {},
    actions: HBoxBuilder.() -> Unit = {},
    content: VBoxBuilder.() -> Unit = {},
): VBox =
    vbox(
        spacing = spacing,
        init = {
            styleClass("hero-banner")
            paddingAll(padding)
            init()
        },
    ) {
        add(
            dev.korafx.dsl.hbox(
                spacing = 16.0,
                init = {
                    styleClass("hero-banner-header")
                    alignment = Pos.CENTER_LEFT
                },
            ) {
                if (icon != null) {
                    add(
                        StackPane(
                            koraIcon(icon, iconSize) {
                                styleClass("hero-banner-icon-glyph")
                            },
                        ).apply {
                            styleClass("hero-banner-icon")
                        },
                    )
                }

                add(
                    dev.korafx.dsl.vbox(
                        spacing = 5.0,
                        init = {
                            styleClass("hero-banner-copy")
                            HBox.setHgrow(this, Priority.ALWAYS)
                        },
                    ) {
                        if (eyebrow != null) {
                            label(eyebrow) {
                                styleClass("hero-banner-eyebrow")
                                isWrapText = true
                            }
                        }

                        label(title) {
                            styleClass("hero-banner-title")
                            isWrapText = true
                        }

                        if (subtitle != null) {
                            label(subtitle) {
                                styleClass("hero-banner-subtitle")
                                isWrapText = true
                            }
                        }
                    },
                )

                add(
                    dev.korafx.dsl.hbox(
                        spacing = 8.0,
                        init = {
                            styleClasses("hero-banner-actions", "action-bar")
                            alignment = Pos.CENTER_RIGHT
                        },
                        content = actions,
                    ),
                )
            },
        )

        vbox(
            spacing = 10.0,
            init = {
                styleClass("hero-banner-content")
            },
            content = content,
        )
    }

fun NodeContainerBuilder.heroBanner(
    title: String,
    subtitle: String? = null,
    eyebrow: String? = null,
    icon: Ikon? = null,
    iconSize: Int = 28,
    spacing: Double = 14.0,
    padding: Double = 22.0,
    init: VBox.() -> Unit = {},
    actions: HBoxBuilder.() -> Unit = {},
    content: VBoxBuilder.() -> Unit = {},
): VBox =
    add(
        dev.korafx.components.heroBanner(
            title = title,
            subtitle = subtitle,
            eyebrow = eyebrow,
            icon = icon,
            iconSize = iconSize,
            spacing = spacing,
            padding = padding,
            init = init,
            actions = actions,
            content = content,
        ),
    )
