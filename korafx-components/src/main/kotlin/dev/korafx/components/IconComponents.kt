package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Labeled
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon

fun koraIcon(
    icon: Ikon,
    size: Int = 16,
    init: FontIcon.() -> Unit = {},
): FontIcon =
    FontIcon(icon).apply {
        iconSize = size
        styleClass("korafx-icon")
        init()
    }

fun iconButton(
    icon: Ikon,
    text: String = "",
    size: Int = 16,
    init: Button.() -> Unit = {},
): Button =
    Button(text).apply {
        styleClasses("icon-button")
        if (text.isBlank()) {
            styleClass("icon-only-button")
        }
        setKoraIcon(icon, size)
        init()
    }

fun Labeled.setKoraIcon(
    icon: Ikon,
    size: Int = 16,
    display: ContentDisplay = ContentDisplay.LEFT,
    gap: Double = 6.0,
    init: FontIcon.() -> Unit = {},
) {
    graphic = koraIcon(icon, size, init)
    contentDisplay = display
    graphicTextGap = gap
}

fun Labeled.clearKoraIcon() {
    graphic = null
}

fun NodeContainerBuilder.koraIcon(
    icon: Ikon,
    size: Int = 16,
    init: FontIcon.() -> Unit = {},
): FontIcon =
    add(dev.korafx.components.koraIcon(icon, size, init))

fun NodeContainerBuilder.iconButton(
    icon: Ikon,
    text: String = "",
    size: Int = 16,
    init: Button.() -> Unit = {},
): Button =
    add(dev.korafx.components.iconButton(icon, text, size, init))
