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
            typography = TypographyTokens(
                fontFamily = "\"Segoe UI\", \"Microsoft YaHei UI\", sans-serif",
                baseSize = 14,
                headlineSize = 28,
            ),
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
            typography = TypographyTokens(
                fontFamily = "\"Segoe UI\", \"Microsoft YaHei UI\", sans-serif",
                baseSize = 14,
                headlineSize = 28,
            ),
            radius = 14,
        ),
    )
}

class ThemeManager(initialTheme: KoraTheme = BuiltInThemes.Light) {
    private val themeState = MutableStateFlow(initialTheme)

    val theme: StateFlow<KoraTheme> = themeState.asStateFlow()

    fun currentTheme(): KoraTheme = themeState.value

    fun setTheme(theme: KoraTheme) {
        themeState.value = theme
    }

    fun toggle() {
        themeState.value = when (themeState.value.id) {
            BuiltInThemes.Dark.id -> BuiltInThemes.Light
            else -> BuiltInThemes.Dark
        }
    }
}

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
