package dev.korafx.macos

import dev.korafx.framework.KoraApplication

object KoraMacosNativeBridge {
    fun install(
        app: KoraApplication,
        spec: KoraMacosChromeSpec,
    ): Boolean {
        if (!isMacOs()) {
            return false
        }

        return runCatching {
            // Native implementation hook:
            // 1. Load a korafx_macos JNI library.
            // 2. Resolve the NSWindow backing this JavaFX Stage.
            // 3. Apply fullSizeContentView, transparent titlebar, and traffic light inset.
            // The Java API is intentionally stable before shipping platform binaries.
            NativeBridgeUnavailable.install(app, spec)
        }.getOrDefault(false)
    }
}

private object NativeBridgeUnavailable {
    fun install(
        app: KoraApplication,
        spec: KoraMacosChromeSpec,
    ): Boolean {
        return false
    }
}
