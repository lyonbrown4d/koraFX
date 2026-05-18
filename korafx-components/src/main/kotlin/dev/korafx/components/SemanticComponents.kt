package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.VBoxBuilder
import dev.korafx.dsl.button
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.kordamp.ikonli.Ikon

enum class ComponentTone(
    internal val styleClass: String,
) {
    NEUTRAL("tone-neutral"),
    PRIMARY("tone-primary"),
    SUCCESS("tone-success"),
    WARNING("tone-warning"),
    DANGER("tone-danger"),
    INFO("tone-info"),
}

fun badge(
    text: String,
    tone: ComponentTone = ComponentTone.NEUTRAL,
    icon: Ikon? = null,
    iconSize: Int = 14,
    init: Label.() -> Unit = {},
): Label =
    Label(text).apply {
        styleClasses("badge", tone.styleClass)
        if (icon != null) {
            setKoraIcon(icon, iconSize)
        }
        init()
    }

fun chip(
    text: String,
    tone: ComponentTone = ComponentTone.NEUTRAL,
    selected: Boolean = false,
    icon: Ikon? = null,
    iconSize: Int = 14,
    init: Button.() -> Unit = {},
): Button =
    Button(text).apply {
        styleClasses("chip", tone.styleClass)
        if (selected) {
            styleClass("chip-selected")
        }
        if (icon != null) {
            setKoraIcon(icon, iconSize)
        }
        init()
    }

fun metricCard(
    label: String,
    value: String,
    helper: String? = null,
    tone: ComponentTone = ComponentTone.NEUTRAL,
    init: VBox.() -> Unit = {},
    content: VBoxBuilder.() -> Unit = {},
): VBox =
    card(
        spacing = 8.0,
        padding = 16.0,
        init = {
            styleClasses("metric-card", tone.styleClass)
            init()
        },
    ) {
        label(label) {
            styleClass("metric-label")
        }
        label(value) {
            styleClass("metric-value")
        }
        if (helper != null) {
            label(helper) {
                styleClass("metric-helper")
                isWrapText = true
            }
        }
        content()
    }

fun alertBanner(
    title: String,
    message: String? = null,
    tone: ComponentTone = ComponentTone.INFO,
    icon: Ikon? = null,
    iconSize: Int = 18,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    init: VBox.() -> Unit = {},
): VBox =
    card(
        spacing = 8.0,
        padding = 14.0,
        init = {
            styleClasses("alert-banner", tone.styleClass)
            init()
        },
    ) {
        label(title) {
            styleClass("alert-title")
            if (icon != null) {
                setKoraIcon(icon, iconSize)
            }
            isWrapText = true
        }
        if (message != null) {
            label(message) {
                styleClass("alert-message")
                isWrapText = true
            }
        }
        if (actionText != null && onAction != null) {
            button(actionText) {
                styleClass("alert-action")
                onAction(onAction)
            }
        }
    }

fun NodeContainerBuilder.badge(
    text: String,
    tone: ComponentTone = ComponentTone.NEUTRAL,
    icon: Ikon? = null,
    iconSize: Int = 14,
    init: Label.() -> Unit = {},
): Label =
    add(dev.korafx.components.badge(text, tone, icon, iconSize, init))

fun NodeContainerBuilder.chip(
    text: String,
    tone: ComponentTone = ComponentTone.NEUTRAL,
    selected: Boolean = false,
    icon: Ikon? = null,
    iconSize: Int = 14,
    init: Button.() -> Unit = {},
): Button =
    add(dev.korafx.components.chip(text, tone, selected, icon, iconSize, init))

fun NodeContainerBuilder.metricCard(
    label: String,
    value: String,
    helper: String? = null,
    tone: ComponentTone = ComponentTone.NEUTRAL,
    init: VBox.() -> Unit = {},
    content: VBoxBuilder.() -> Unit = {},
): VBox =
    add(
        dev.korafx.components.metricCard(
            label = label,
            value = value,
            helper = helper,
            tone = tone,
            init = init,
            content = content,
        ),
    )

fun NodeContainerBuilder.alertBanner(
    title: String,
    message: String? = null,
    tone: ComponentTone = ComponentTone.INFO,
    icon: Ikon? = null,
    iconSize: Int = 18,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    init: VBox.() -> Unit = {},
): VBox =
    add(
        dev.korafx.components.alertBanner(
            title = title,
            message = message,
            tone = tone,
            icon = icon,
            iconSize = iconSize,
            actionText = actionText,
            onAction = onAction,
            init = init,
        ),
    )
