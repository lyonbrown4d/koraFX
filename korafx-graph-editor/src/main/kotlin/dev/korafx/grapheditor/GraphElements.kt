package dev.korafx.grapheditor

data class GraphMetadata(
    val values: MutableMap<String, String> = LinkedHashMap(),
) {
    fun set(
        key: String,
        value: String,
    ): String? = values.put(key, value)

    fun get(key: String): String? = values[key]

    fun remove(key: String): String? = values.remove(key)
}

data class GraphNode(
    val id: String,
    var label: String,
    var x: Double,
    var y: Double,
    var width: Double = 140.0,
    var height: Double = 64.0,
    val metadata: MutableMap<String, String> = LinkedHashMap(),
)

data class GraphEdge(
    val id: String,
    val sourceId: String,
    val targetId: String,
    var label: String? = null,
    val metadata: MutableMap<String, String> = LinkedHashMap(),
)
