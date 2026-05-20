package dev.korafx.navigation

data class NavigationResultKey<T : Any>(
    val name: String,
) {
    init {
        require(name.isNotBlank()) {
            "Navigation result key cannot be blank."
        }
    }
}

fun <T : Any> navigationResultKey(name: String): NavigationResultKey<T> =
    NavigationResultKey(name)
