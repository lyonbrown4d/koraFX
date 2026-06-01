package dev.korafx.framework.theme

import javafx.scene.Scene
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.nio.file.Files
import java.util.LinkedHashMap

object BuiltInThemes {
    private val desktopTypography = TypographyTokens(
        fontFamily = "\"Roboto\", \"Segoe UI\", \"Microsoft YaHei UI\", sans-serif",
        baseSize = 14,
        headlineSize = 28,
    )

    private val fluentTypography = TypographyTokens(
        fontFamily = "\"Segoe UI\", \"Microsoft YaHei UI\", sans-serif",
        baseSize = 14,
        headlineSize = 26,
    )

    val MaterialLight = KoraTheme(
        id = "material-light",
        displayName = "Material Light",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#6750A4",
                success = "#2E7D32",
                warning = "#F57C00",
                danger = "#B3261E",
                info = "#006EA6",
                surface = "#FFFBFE",
                surfaceMuted = "#FFFFFF",
                textPrimary = "#1C1B1F",
                textSecondary = "#49454F",
                border = "#CAC4D0",
            ),
            typography = desktopTypography,
            radius = 12,
            states = StateColorTokens(
                controlHover = "#5B4695",
                controlPressed = "#4F3D84",
                surfaceHover = "#F4EFF7",
                rowHover = "#F2EAFB",
                rowAlternate = "#F7F2FA",
                selected = "#6750A4",
                selectedText = "#FFFFFF",
                focus = "#6750A4",
                invalid = "#B3261E",
                scrollbarThumb = "#938F99",
            ),
        ),
    )

    val MaterialDark = KoraTheme(
        id = "material-dark",
        displayName = "Material Dark",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#D0BCFF",
                success = "#81C784",
                warning = "#FFD54F",
                danger = "#F2B8B5",
                info = "#8FD9FF",
                surface = "#1C1B1F",
                surfaceMuted = "#2B2930",
                textPrimary = "#E6E1E5",
                textSecondary = "#CAC4D0",
                border = "#49454F",
            ),
            typography = desktopTypography,
            radius = 12,
            states = StateColorTokens(
                controlHover = "#E9DDFF",
                controlPressed = "#F6EDFF",
                surfaceHover = "#36343B",
                rowHover = "#3A3446",
                rowAlternate = "#242128",
                selected = "#D0BCFF",
                selectedText = "#381E72",
                focus = "#D0BCFF",
                invalid = "#F2B8B5",
                scrollbarThumb = "#6F6A75",
            ),
        ),
    )

    val FluentLight = KoraTheme(
        id = "fluent-light",
        displayName = "Fluent Light",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#0078D4",
                success = "#107C10",
                warning = "#F7630C",
                danger = "#A4262C",
                info = "#0078D4",
                surface = "#F3F2F1",
                surfaceMuted = "#FFFFFF",
                textPrimary = "#201F1E",
                textSecondary = "#605E5C",
                border = "#D2D0CE",
            ),
            typography = fluentTypography,
            radius = 4,
            radii = RadiusTokens(small = 2, medium = 4, large = 8),
            states = StateColorTokens(
                controlHover = "#106EBE",
                controlPressed = "#005A9E",
                surfaceHover = "#EDEBE9",
                rowHover = "#EFF6FC",
                rowAlternate = "#FAF9F8",
                selected = "#0078D4",
                selectedText = "#FFFFFF",
                focus = "#0078D4",
                invalid = "#A4262C",
                scrollbarThumb = "#C8C6C4",
            ),
            elevation = ElevationTokens(
                card = "dropshadow(gaussian, rgba(0, 0, 0, 0.08), 8, 0.10, 0, 2)",
                dropdown = "dropshadow(gaussian, rgba(0, 0, 0, 0.18), 16, 0.14, 0, 4)",
                modal = "dropshadow(gaussian, rgba(0, 0, 0, 0.24), 24, 0.18, 0, 8)",
                snackbar = "dropshadow(gaussian, rgba(0, 0, 0, 0.18), 12, 0.12, 0, 4)",
            ),
        ),
    )

    val FluentDark = KoraTheme(
        id = "fluent-dark",
        displayName = "Fluent Dark",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#2899F5",
                success = "#6CCB5F",
                warning = "#FCE100",
                danger = "#FF99A4",
                info = "#60CDFF",
                surface = "#202020",
                surfaceMuted = "#2B2B2B",
                textPrimary = "#F3F2F1",
                textSecondary = "#C8C6C4",
                border = "#3B3A39",
            ),
            typography = fluentTypography,
            radius = 4,
            radii = RadiusTokens(small = 2, medium = 4, large = 8),
            states = StateColorTokens(
                controlHover = "#3AA0F3",
                controlPressed = "#0F6CBD",
                surfaceHover = "#323130",
                rowHover = "#1E3A5F",
                rowAlternate = "#252423",
                selected = "#2899F5",
                selectedText = "#000000",
                focus = "#60CDFF",
                invalid = "#FF99A4",
                scrollbarThumb = "#605E5C",
            ),
            elevation = ElevationTokens(
                card = "dropshadow(gaussian, rgba(0, 0, 0, 0.28), 8, 0.14, 0, 2)",
                dropdown = "dropshadow(gaussian, rgba(0, 0, 0, 0.38), 18, 0.18, 0, 5)",
                modal = "dropshadow(gaussian, rgba(0, 0, 0, 0.48), 28, 0.22, 0, 10)",
                snackbar = "dropshadow(gaussian, rgba(0, 0, 0, 0.42), 14, 0.18, 0, 4)",
            ),
        ),
    )

    val all: List<KoraTheme> = listOf(MaterialLight, MaterialDark, FluentLight, FluentDark)

    val byId: Map<String, KoraTheme> = all.associateBy(KoraTheme::id)

    fun findById(id: String): KoraTheme? = byId[id]

    fun requireById(id: String): KoraTheme =
        findById(id) ?: error("Unknown built-in KoraFX theme id: $id")
}

object ThemeStyleClass {
    const val Root = "korafx-root"
    const val Headline = "headline"
    const val Muted = "muted"
}

class ThemeManager(
    initialTheme: KoraTheme = BuiltInThemes.MaterialLight,
    val availableThemes: List<KoraTheme> = BuiltInThemes.all,
) {
    private val themeState = MutableStateFlow(initialTheme)

    init {
        require(availableThemes.isNotEmpty()) {
            "ThemeManager requires at least one available theme."
        }
    }

    val theme: StateFlow<KoraTheme> = themeState.asStateFlow()

    fun currentTheme(): KoraTheme = themeState.value

    fun setTheme(theme: KoraTheme) {
        themeState.value = theme
    }

    fun setTheme(id: String) {
        setTheme(
            availableThemes.firstOrNull { it.id == id }
                ?: error("Unknown available KoraFX theme id: $id"),
        )
    }

    fun nextTheme() {
        themeState.value = availableThemes.nextAfter(themeState.value)
    }

    fun previousTheme() {
        themeState.value = availableThemes.previousBefore(themeState.value)
    }

    fun toggle() {
        if (availableThemes.size < 2) {
            return
        }

        val first = availableThemes[0]
        val second = availableThemes[1]
        themeState.value = if (themeState.value.id == first.id) second else first
    }
}

private fun List<KoraTheme>.nextAfter(theme: KoraTheme): KoraTheme {
    val index = indexOfFirst { it.id == theme.id }
    return this[(index + 1).floorMod(size)]
}

private fun List<KoraTheme>.previousBefore(theme: KoraTheme): KoraTheme {
    val index = indexOfFirst { it.id == theme.id }
    if (index == -1) {
        return last()
    }
    return this[(index - 1).floorMod(size)]
}

private fun Int.floorMod(modulus: Int): Int =
    ((this % modulus) + modulus) % modulus

class SceneThemeController(
    private val themeManager: ThemeManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.JavaFx)
    private val stylesheetCache = object : LinkedHashMap<String, Path>(
        SceneThemeCacheSize,
        0.75f,
        true,
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Path>): Boolean {
            if (size <= SceneThemeCacheSize) {
                return false
            }

            runCatching {
                Files.deleteIfExists(eldest.value)
            }
            return true
        }
    }

    fun bind(scene: Scene) {
        if (!scene.root.styleClass.contains(ThemeStyleClass.Root)) {
            scene.root.styleClass += ThemeStyleClass.Root
        }

        scope.launch {
            themeManager.theme.collectLatest { theme ->
                val stylesheetUri = stylesheetCache.getOrPut(theme.cacheKey()) { writeStylesheet(theme) }
                stylesheetUri.toFile().deleteOnExit()
                scene.stylesheets.setAll(stylesheetUri.toUri().toString())
            }
        }
    }

    private fun writeStylesheet(theme: KoraTheme): Path {
        val file = Files.createTempFile("korafx-${theme.id}-", ".css")
        Files.writeString(file, ThemeStylesheetFactory.render(theme))
        return file
    }

    fun dispose() {
        scope.cancel()
        stylesheetCache.values.forEach { path ->
            runCatching {
                Files.deleteIfExists(path)
            }
        }
        stylesheetCache.clear()
    }
}

private fun KoraTheme.cacheKey(): String =
    "${id}:${tokens.hashCode()}"

private const val SceneThemeCacheSize = 16
