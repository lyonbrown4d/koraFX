package dev.korafx.theme

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
import java.nio.file.Files

data class ColorTokens(
    val primary: String,
    val surface: String,
    val surfaceMuted: String,
    val textPrimary: String,
    val textSecondary: String,
    val border: String,
)

data class TypographyTokens(
    val fontFamily: String,
    val baseSize: Int,
    val headlineSize: Int,
)

data class ThemeTokens(
    val colors: ColorTokens,
    val typography: TypographyTokens,
    val radius: Int,
)

data class KoraTheme(
    val id: String,
    val displayName: String,
    val tokens: ThemeTokens,
)

object BuiltInThemes {
    private val desktopTypography = TypographyTokens(
        fontFamily = "\"Segoe UI\", \"Microsoft YaHei UI\", sans-serif",
        baseSize = 14,
        headlineSize = 28,
    )

    val Light = KoraTheme(
        id = "light",
        displayName = "Light",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#246BFD",
                surface = "#F6F7FB",
                surfaceMuted = "#FFFFFF",
                textPrimary = "#111827",
                textSecondary = "#4B5563",
                border = "#D9E0EE",
            ),
            typography = desktopTypography,
            radius = 14,
        ),
    )

    val Dark = KoraTheme(
        id = "dark",
        displayName = "Dark",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#7AA2FF",
                surface = "#161B25",
                surfaceMuted = "#202735",
                textPrimary = "#F3F6FB",
                textSecondary = "#AAB6CC",
                border = "#2F394D",
            ),
            typography = desktopTypography,
            radius = 14,
        ),
    )

    val Slate = KoraTheme(
        id = "slate",
        displayName = "Slate",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#475569",
                surface = "#F1F5F9",
                surfaceMuted = "#FFFFFF",
                textPrimary = "#0F172A",
                textSecondary = "#475569",
                border = "#CBD5E1",
            ),
            typography = desktopTypography,
            radius = 10,
        ),
    )

    val Nord = KoraTheme(
        id = "nord",
        displayName = "Nord",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#5E81AC",
                surface = "#ECEFF4",
                surfaceMuted = "#FFFFFF",
                textPrimary = "#2E3440",
                textSecondary = "#4C566A",
                border = "#D8DEE9",
            ),
            typography = desktopTypography,
            radius = 12,
        ),
    )

    val SolarizedLight = KoraTheme(
        id = "solarized-light",
        displayName = "Solarized Light",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#268BD2",
                surface = "#FDF6E3",
                surfaceMuted = "#EEE8D5",
                textPrimary = "#073642",
                textSecondary = "#657B83",
                border = "#D6CCB5",
            ),
            typography = desktopTypography,
            radius = 8,
        ),
    )

    val GraphiteDark = KoraTheme(
        id = "graphite-dark",
        displayName = "Graphite Dark",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#A78BFA",
                surface = "#111318",
                surfaceMuted = "#1A1D24",
                textPrimary = "#F8FAFC",
                textSecondary = "#B6C2D1",
                border = "#343A46",
            ),
            typography = desktopTypography,
            radius = 12,
        ),
    )

    val Forest = KoraTheme(
        id = "forest",
        displayName = "Forest",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#15803D",
                surface = "#F0F8F1",
                surfaceMuted = "#FFFFFF",
                textPrimary = "#102417",
                textSecondary = "#486451",
                border = "#C8DDCC",
            ),
            typography = desktopTypography,
            radius = 14,
        ),
    )

    val Rose = KoraTheme(
        id = "rose",
        displayName = "Rose",
        tokens = ThemeTokens(
            colors = ColorTokens(
                primary = "#E11D48",
                surface = "#FFF1F2",
                surfaceMuted = "#FFFFFF",
                textPrimary = "#3F0B16",
                textSecondary = "#7F4A55",
                border = "#F7C8D0",
            ),
            typography = desktopTypography,
            radius = 18,
        ),
    )

    val all: List<KoraTheme> = listOf(
        Light,
        Dark,
        Slate,
        Nord,
        SolarizedLight,
        GraphiteDark,
        Forest,
        Rose,
    )

    val byId: Map<String, KoraTheme> = all.associateBy(KoraTheme::id)

    fun findById(id: String): KoraTheme? = byId[id]

    fun requireById(id: String): KoraTheme =
        findById(id) ?: error("Unknown built-in KoraFX theme id: $id")
}

class ThemeManager(
    initialTheme: KoraTheme = BuiltInThemes.Light,
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
        themeState.value = when (themeState.value.id) {
            BuiltInThemes.Dark.id -> BuiltInThemes.Light
            else -> BuiltInThemes.Dark
        }
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
    private val cache = linkedMapOf<String, String>()

    fun bind(scene: Scene) {
        if (!scene.root.styleClass.contains("korafx-root")) {
            scene.root.styleClass += "korafx-root"
        }

        scope.launch {
            themeManager.theme.collectLatest { theme ->
                val stylesheetUri = cache.getOrPut(theme.id) { writeStylesheet(theme) }
                scene.stylesheets.setAll(stylesheetUri)
            }
        }
    }

    private fun writeStylesheet(theme: KoraTheme): String {
        val file = Files.createTempFile("korafx-${theme.id}-", ".css")
        Files.writeString(file, ThemeStylesheetFactory.render(theme))
        file.toFile().deleteOnExit()
        return file.toUri().toString()
    }

    fun dispose() {
        scope.cancel()
    }
}
