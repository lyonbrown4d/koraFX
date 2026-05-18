package dev.korafx.dsl

import javafx.scene.Node

@DslMarker
private annotation class CssStyleDsl

class CssStyle internal constructor(
    val declarations: Map<String, String>,
) {
    fun asCssText(): String =
        declarations.entries.joinToString("; ") { "${it.key}: ${it.value}" }.let { rendered ->
            if (rendered.isBlank()) "" else "$rendered;"
        }

    override fun toString(): String = asCssText()

    override fun equals(other: Any?): Boolean =
        this === other || (other is CssStyle && declarations == other.declarations)

    override fun hashCode(): Int = declarations.hashCode()
}

@CssStyleDsl
class CssStyleBuilder {
    private val declarations = linkedMapOf<String, String>()

    private fun setProperty(
        name: String,
        value: String,
    ) {
        declarations[normalizePropertyName(name)] = value
    }

    private fun removeProperty(name: String) {
        declarations.remove(normalizePropertyName(name))
    }

    private fun setNamedProperty(name: String, value: String?) {
        if (value == null) {
            removeProperty(name)
            return
        }
        setProperty(name, value)
    }

    private fun setPixelProperty(name: String, value: Double) {
        setProperty(name, value.toPx())
    }

    private fun Double.toPx(): String = if (this % 1.0 == 0.0) "${toInt()}px" else "${toString()}px"

    fun fx(
        name: String,
        value: String,
    ) {
        setProperty(name, value)
    }

    fun raw(declaration: String) {
        declaration.split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { item ->
                val parts = item.split(":", limit = 2).map { it.trim() }
                if (parts.size == 2 && parts[0].isNotBlank()) {
                    declarations[normalizePropertyName(parts[0])] = parts[1]
                }
            }
    }

    private fun normalizePropertyName(name: String): String = if (name.startsWith("-fx-")) name else "-fx-$name"

    fun backgroundColor(value: String) {
        setProperty("background-color", value)
    }

    fun textFill(value: String) {
        setProperty("text-fill", value)
    }

    fun borderColor(value: String) {
        setProperty("border-color", value)
    }

    fun borderRadius(value: Double) {
        setPixelProperty("border-radius", value)
    }

    fun backgroundRadius(value: Double) {
        setPixelProperty("background-radius", value)
    }

    fun backgroundInsets(value: Double) {
        setPixelProperty("background-insets", value)
    }

    fun borderWidth(value: Double) {
        setPixelProperty("border-width", value)
    }

    fun fontSize(value: Double) {
        setPixelProperty("font-size", value)
    }

    fun fontFamily(value: String) {
        setProperty("font-family", value)
    }

    fun fontWeight(value: String) {
        setProperty("font-weight", value)
    }

    fun opacity(value: Double) {
        require(value in 0.0..1.0) {
            "Opacity must be between 0.0 and 1.0."
        }
        setProperty("opacity", value.toString())
    }

    fun cursor(value: String) {
        setProperty("cursor", value)
    }

    fun textAlignment(value: String) {
        setProperty("text-alignment", value)
    }

    fun alignment(value: String) {
        setProperty("alignment", value)
    }

    fun padding(all: Double) {
        padding(all, all, all, all)
    }

    fun padding(vertical: Double, horizontal: Double) {
        setProperty("padding", "${vertical.toPx()} ${horizontal.toPx()}")
    }

    fun padding(
        top: Double,
        right: Double,
        bottom: Double,
        left: Double,
    ) {
        setProperty("padding", "${top.toPx()} ${right.toPx()} ${bottom.toPx()} ${left.toPx()}")
    }

    fun radius(value: Double) {
        setProperty("background-radius", value.toPx())
        setProperty("border-radius", value.toPx())
        setPixelProperty("background-insets", 0.0)
    }

    fun background(
        background: String?,
        border: String?,
        radius: Double,
    ) {
        background?.let(::backgroundColor)
        setNamedProperty("border-color", border)
        radius(radius)
    }

    fun build(): CssStyle = CssStyle(declarations.toMap())
}

fun cssStyleOf(init: CssStyleBuilder.() -> Unit): CssStyle = CssStyleBuilder().apply(init).build()

fun Node.cssStyle(style: CssStyle) {
    this.style = style.asCssText()
}

fun Node.cssStyle(init: CssStyleBuilder.() -> Unit) {
    cssStyle(cssStyleOf(init))
}

fun Node.cssAppend(style: CssStyle) {
    val rendered = style.asCssText()
    if (rendered.isEmpty()) {
        return
    }

    val currentStyle = this.style.trim()
        .trimEnd(';')
    this.style = if (currentStyle.isBlank()) {
        rendered
    } else {
        "$currentStyle; $rendered"
    }
}

fun Node.cssAppend(init: CssStyleBuilder.() -> Unit) {
    cssAppend(cssStyleOf(init))
}

fun Node.styleRaw(raw: String) {
    style = raw
}
