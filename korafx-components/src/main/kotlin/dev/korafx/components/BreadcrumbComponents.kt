package dev.korafx.components

import dev.korafx.dsl.HBoxBuilder
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.VBoxBuilder
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.vbox
import javafx.geometry.Pos
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.kordamp.ikonli.Ikon

data class BreadcrumbItem<T>(
    val value: T,
    val text: String,
    val current: Boolean = false,
    val disabled: Boolean = false,
    val icon: Ikon? = null,
)

fun <T> breadcrumbItem(
    value: T,
    text: String,
    current: Boolean = false,
    disabled: Boolean = false,
    icon: Ikon? = null,
): BreadcrumbItem<T> =
    BreadcrumbItem(
        value = value,
        text = text,
        current = current,
        disabled = disabled,
        icon = icon,
    )

fun <T> breadcrumb(
    items: Iterable<BreadcrumbItem<T>>,
    separator: String = "/",
    iconSize: Int = 14,
    onSelect: (T) -> Unit = {},
    init: HBox.() -> Unit = {},
): HBox =
    HBox(6.0).apply {
        styleClass("breadcrumb")
        alignment = Pos.CENTER_LEFT
        init()

        items.forEachIndexed { index, item ->
            if (index > 0) {
                children += Label(separator).apply {
                    styleClass("breadcrumb-separator")
                }
            }

            children += item.toNode(
                iconSize = iconSize,
                onSelect = onSelect,
            )
        }
    }

fun pageHeader(
    title: String,
    subtitle: String? = null,
    eyebrow: String? = null,
    icon: Ikon? = null,
    iconSize: Int = 22,
    init: VBox.() -> Unit = {},
    actions: HBoxBuilder.() -> Unit = {},
    content: VBoxBuilder.() -> Unit = {},
): VBox =
    vbox(
        spacing = 10.0,
        init = {
            styleClass("page-header")
            init()
        },
    ) {
        if (eyebrow != null) {
            label(eyebrow) {
                styleClass("page-header-eyebrow")
            }
        }

        add(
            dev.korafx.dsl.hbox(
                spacing = 12.0,
                init = {
                    styleClass("page-header-title-row")
                    alignment = Pos.CENTER_LEFT
                },
            ) {
                label(title) {
                    styleClass("page-header-title")
                    if (icon != null) {
                        setKoraIcon(icon, iconSize)
                    }
                    isWrapText = true
                }
                add(Region().apply {
                    HBox.setHgrow(this, Priority.ALWAYS)
                })
                add(
                    dev.korafx.dsl.hbox(
                        spacing = 8.0,
                        init = {
                            styleClass("page-header-actions")
                            alignment = Pos.CENTER_RIGHT
                        },
                        content = actions,
                    ),
                )
            },
        )

        if (subtitle != null) {
            label(subtitle) {
                styleClass("page-header-subtitle")
                isWrapText = true
            }
        }

        vbox(
            spacing = 8.0,
            init = {
                styleClass("page-header-content")
            },
            content = content,
        )
    }

fun <T> NodeContainerBuilder.breadcrumb(
    items: Iterable<BreadcrumbItem<T>>,
    separator: String = "/",
    iconSize: Int = 14,
    onSelect: (T) -> Unit = {},
    init: HBox.() -> Unit = {},
): HBox =
    add(
        dev.korafx.components.breadcrumb(
            items = items,
            separator = separator,
            iconSize = iconSize,
            onSelect = onSelect,
            init = init,
        ),
    )

fun NodeContainerBuilder.pageHeader(
    title: String,
    subtitle: String? = null,
    eyebrow: String? = null,
    icon: Ikon? = null,
    iconSize: Int = 22,
    init: VBox.() -> Unit = {},
    actions: HBoxBuilder.() -> Unit = {},
    content: VBoxBuilder.() -> Unit = {},
): VBox =
    add(
        dev.korafx.components.pageHeader(
            title = title,
            subtitle = subtitle,
            eyebrow = eyebrow,
            icon = icon,
            iconSize = iconSize,
            init = init,
            actions = actions,
            content = content,
        ),
    )

private fun <T> BreadcrumbItem<T>.toNode(
    iconSize: Int,
    onSelect: (T) -> Unit,
) =
    if (current || disabled) {
        Label(text).apply {
            styleClasses("breadcrumb-item", if (current) "breadcrumb-item-current" else "breadcrumb-item-disabled")
            isDisable = disabled
            icon?.let { setKoraIcon(it, iconSize) }
        }
    } else {
        Hyperlink(text).apply {
            styleClasses("breadcrumb-item", "breadcrumb-item-link")
            icon?.let { setKoraIcon(it, iconSize) }
            onAction {
                onSelect(value)
            }
        }
    }
