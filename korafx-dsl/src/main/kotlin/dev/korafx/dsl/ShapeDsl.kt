package dev.korafx.dsl

import javafx.scene.shape.Rectangle

fun rectangle(
    width: Double = 0.0,
    height: Double = 0.0,
    init: Rectangle.() -> Unit = {},
): Rectangle =
    Rectangle(width, height).apply(init)

fun rectangle(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    init: Rectangle.() -> Unit = {},
): Rectangle =
    Rectangle(x, y, width, height).apply(init)

fun NodeContainerBuilder.rectangle(
    width: Double = 0.0,
    height: Double = 0.0,
    init: Rectangle.() -> Unit = {},
): Rectangle =
    add(dev.korafx.dsl.rectangle(width, height, init))

fun NodeContainerBuilder.rectangle(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    init: Rectangle.() -> Unit = {},
): Rectangle =
    add(dev.korafx.dsl.rectangle(x, y, width, height, init))
