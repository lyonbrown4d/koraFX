package dev.korafx.devtools

internal interface DevtoolsActions {
    fun startPicking()

    fun clearSelection()

    fun setPlacement(placement: KoraDevtoolsPlacement)
}
