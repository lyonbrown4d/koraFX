package dev.korafx.grapheditor

internal fun Graph.addNodeCopy(node: GraphNode) {
    addNode(
        id = node.id,
        label = node.label,
        x = node.x,
        y = node.y,
        width = node.width,
        height = node.height,
        metadata = node.metadata,
    )
}

internal fun Graph.addEdgeCopy(edge: GraphEdge) {
    addEdge(
        sourceId = edge.sourceId,
        targetId = edge.targetId,
        id = edge.id,
        label = edge.label,
        metadata = edge.metadata,
    )
}

internal fun nextGraphId(
    requested: String,
    defaultBase: () -> String,
    exists: (String) -> Boolean,
): String {
    val base = if (requested.isBlank()) defaultBase() else requested
    if (!exists(base)) {
        return base
    }

    var candidate = base
    var suffix = 2
    if (candidate.contains("-")) {
        val split = candidate.lastIndexOf('-')
        val number = candidate.substring(split + 1).toIntOrNull()
        if (number != null) {
            candidate = candidate.substring(0, split)
            suffix = number + 1
        }
    }

    while (exists("${candidate}-$suffix")) {
        suffix += 1
    }
    return "${candidate}-$suffix"
}
