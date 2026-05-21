package dev.korafx.macos

import dev.korafx.framework.KoraApplication
import dev.korafx.framework.KoraApplicationBuilder
import dev.korafx.framework.KoraApplicationPlugin
import dev.korafx.framework.KoraWindowChromeMode
import dev.korafx.framework.KoraWindowTitleBarBuilder
import javafx.application.Platform

data class KoraMacosChromeSpec(
    val preserveTrafficLights: Boolean = true,
    val fullSizeContentView: Boolean = true,
    val transparentTitlebar: Boolean = true,
    val trafficLightInsetX: Double = 14.0,
    val trafficLightInsetY: Double = 12.0,
)

class KoraMacosChromeBuilder {
    var preserveTrafficLights: Boolean = true
    var fullSizeContentView: Boolean = true
    var transparentTitlebar: Boolean = true
    var trafficLightInsetX: Double = 14.0
    var trafficLightInsetY: Double = 12.0

    fun trafficLightInset(
        x: Double,
        y: Double,
    ) {
        trafficLightInsetX = x
        trafficLightInsetY = y
    }

    internal fun build(): KoraMacosChromeSpec {
        require(trafficLightInsetX >= 0.0 && trafficLightInsetY >= 0.0) {
            "KoraFX macOS traffic light inset values must be non-negative."
        }

        return KoraMacosChromeSpec(
            preserveTrafficLights = preserveTrafficLights,
            fullSizeContentView = fullSizeContentView,
            transparentTitlebar = transparentTitlebar,
            trafficLightInsetX = trafficLightInsetX,
            trafficLightInsetY = trafficLightInsetY,
        )
    }
}

fun KoraApplicationBuilder.installMacosChrome(
    configure: KoraMacosChromeBuilder.() -> Unit = {},
) {
    install(KoraMacosChromePlugin(KoraMacosChromeBuilder().apply(configure).build()))
}

fun KoraWindowTitleBarBuilder.macos(
    configure: KoraMacosChromeBuilder.() -> Unit = {},
) {
    chromeMode = KoraWindowChromeMode.NATIVE_OVERLAY
    nativeOption(MacosChromeOptionKey, KoraMacosChromeBuilder().apply(configure).build())
}

class KoraMacosChromePlugin(
    private val fallbackSpec: KoraMacosChromeSpec = KoraMacosChromeSpec(),
) : KoraApplicationPlugin {
    override fun onStart(app: KoraApplication) {
        if (!isMacOs() || app.window.titleBar.chromeMode != KoraWindowChromeMode.NATIVE_OVERLAY) {
            return
        }

        val spec = app.window.titleBar.nativeOption<KoraMacosChromeSpec>(MacosChromeOptionKey) ?: fallbackSpec
        Platform.runLater {
            KoraMacosNativeBridge.install(app, spec)
        }
    }
}

internal const val MacosChromeOptionKey = "dev.korafx.macos.chrome"

internal fun isMacOs(): Boolean =
    System.getProperty("os.name").contains("mac", ignoreCase = true)
