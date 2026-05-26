package dev.korafx.devtools

import javafx.geometry.Orientation

internal fun KoraDevtoolsPlacement.dockOrientation(): Orientation =
    when (this) {
        KoraDevtoolsPlacement.BOTTOM -> Orientation.VERTICAL
        KoraDevtoolsPlacement.LEFT,
        KoraDevtoolsPlacement.RIGHT,
        KoraDevtoolsPlacement.WINDOW,
        -> Orientation.HORIZONTAL
    }

internal fun KoraDevtoolsPlacement.initialDivider(
    spec: KoraDevtoolsSpec,
    sceneWidth: Double,
    sceneHeight: Double,
): Double =
    when (this) {
        KoraDevtoolsPlacement.LEFT ->
            (spec.dockWidth / sceneWidth.validSize()).coerceIn(0.15, 0.85)
        KoraDevtoolsPlacement.RIGHT ->
            ((sceneWidth.validSize() - spec.dockWidth) / sceneWidth.validSize()).coerceIn(0.15, 0.85)
        KoraDevtoolsPlacement.BOTTOM ->
            ((sceneHeight.validSize() - spec.dockHeight) / sceneHeight.validSize()).coerceIn(0.15, 0.85)
        KoraDevtoolsPlacement.WINDOW -> 0.5
    }

private fun Double.validSize(): Double =
    takeIf { it.isFinite() && it > 0.0 } ?: 1.0
