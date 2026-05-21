package dev.korafx.grapheditor

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.collections.ObservableList

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

class Graph(
    initialNodes: Iterable<GraphNode> = emptyList(),
    initialEdges: Iterable<GraphEdge> = emptyList(),
    val metadata: GraphMetadata = GraphMetadata(),
) {
    private val _nodes = FXCollections.observableArrayList<GraphNode>()
    private val _edges = FXCollections.observableArrayList<GraphEdge>()

    private val _selectedNode = ReadOnlyObjectWrapper<GraphNode?>(null)
    private val _selectedEdge = ReadOnlyObjectWrapper<GraphEdge?>(null)
    private val _connectingFrom = ReadOnlyObjectWrapper<GraphNode?>(null)

    val selectedNodeProperty: ReadOnlyObjectProperty<GraphNode?> = _selectedNode.readOnlyProperty
    val selectedEdgeProperty: ReadOnlyObjectProperty<GraphEdge?> = _selectedEdge.readOnlyProperty
    val connectingFromProperty: ReadOnlyObjectProperty<GraphNode?> = _connectingFrom.readOnlyProperty

    val nodes: ObservableList<GraphNode> = FXCollections.unmodifiableObservableList(_nodes)
    val edges: ObservableList<GraphEdge> = FXCollections.unmodifiableObservableList(_edges)

    private val nodeCreatedHandlers = mutableListOf<(GraphNode) -> Unit>()
    private val edgeCreatedHandlers = mutableListOf<(GraphEdge) -> Unit>()

    val selectedNode: GraphNode?
        get() = selectedNodeProperty.value

    val selectedEdge: GraphEdge?
        get() = selectedEdgeProperty.value

    val connectingFrom: GraphNode?
        get() = connectingFromProperty.value

    var allowSelfEdges: Boolean = false
        set(value) {
            field = value
            if (!value) {
                _edges.removeIf { it.sourceId == it.targetId }
            }
        }

    private var nodeSequence = 0
    private var edgeSequence = 0

    init {
        initialNodes.forEach {
            addNode(
                id = it.id,
                label = it.label,
                x = it.x,
                y = it.y,
                width = it.width,
                height = it.height,
                metadata = it.metadata,
            )
        }
        initialEdges.forEach {
            addEdge(
                sourceId = it.sourceId,
                targetId = it.targetId,
                id = it.id,
                label = it.label,
                metadata = it.metadata,
            )
        }
    }

    fun clearSelectionAndConnections() {
        clearSelection()
        cancelConnection()
    }

    fun reset(
        nodes: Iterable<GraphNode>,
        edges: Iterable<GraphEdge>,
    ) {
        clear()
        nodes.forEach {
            addNode(
                id = it.id,
                label = it.label,
                x = it.x,
                y = it.y,
                width = it.width,
                height = it.height,
                metadata = it.metadata,
            )
        }
        edges.forEach {
            addEdge(
                sourceId = it.sourceId,
                targetId = it.targetId,
                id = it.id,
                label = it.label,
                metadata = it.metadata,
            )
        }
    }

    fun setMetadata(
        key: String,
        value: String,
    ) {
        metadata.set(key, value)
    }

    fun nodeOf(id: String): GraphNode? =
        _nodes.firstOrNull { it.id == id }

    fun edgeOf(id: String): GraphEdge? =
        _edges.firstOrNull { it.id == id }

    fun addNode(
        id: String? = null,
        label: String = "Node",
        x: Double = 40.0,
        y: Double = 40.0,
        width: Double = 140.0,
        height: Double = 64.0,
        metadata: Map<String, String> = emptyMap(),
    ): GraphNode {
        val prepared = GraphNode(
            id = nextNodeId(id ?: ""),
            label = label,
            x = x,
            y = y,
            width = width,
            height = height,
            metadata = LinkedHashMap(metadata),
        )
        _nodes += prepared
        nodeCreatedHandlers.forEach { it(prepared) }
        return prepared
    }

    fun setNodePosition(
        node: GraphNode,
        x: Double,
        y: Double,
    ) {
        moveNode(node, x, y)
    }

    fun moveNode(
        node: GraphNode,
        x: Double,
        y: Double,
    ) {
        if (_nodes.contains(node)) {
            node.x = x
            node.y = y
        }
    }

    fun setNodeLabel(
        node: GraphNode,
        label: String,
    ) {
        if (_nodes.contains(node)) {
            node.label = label
        }
    }

    fun removeNode(node: GraphNode) {
        if (_nodes.remove(node)) {
            _edges.removeAll(_edges.filter { it.sourceId == node.id || it.targetId == node.id })
            if (selectedNode == node) {
                clearSelection()
            }
            if (connectingFrom == node) {
                _connectingFrom.set(null)
            }
        }
    }

    fun removeNodeById(id: String) {
        nodeOf(id)?.let(::removeNode)
    }

    fun addEdge(
        source: GraphNode,
        target: GraphNode,
        id: String? = null,
        label: String? = null,
        metadata: Map<String, String> = emptyMap(),
    ): GraphEdge =
        addEdge(
            sourceId = source.id,
            targetId = target.id,
            id = id,
            label = label,
            metadata = metadata,
        )

    fun addEdge(
        sourceId: String,
        targetId: String,
        id: String? = null,
        label: String? = null,
        metadata: Map<String, String> = emptyMap(),
    ): GraphEdge {
        require(nodeOf(sourceId) != null) { "Source node not found: $sourceId" }
        require(nodeOf(targetId) != null) { "Target node not found: $targetId" }
        if (!allowSelfEdges && sourceId == targetId) {
            throw IllegalArgumentException("Self-edge is disabled.")
        }

        val prepared = GraphEdge(
            id = nextEdgeId(id ?: ""),
            sourceId = sourceId,
            targetId = targetId,
            label = label,
            metadata = LinkedHashMap(metadata),
        )
        _edges += prepared
        edgeCreatedHandlers.forEach { it(prepared) }
        return prepared
    }

    fun removeEdge(edge: GraphEdge) {
        if (_edges.remove(edge) && selectedEdge == edge) {
            clearSelection()
        }
    }

    fun removeEdgeById(id: String) {
        edgeOf(id)?.let(::removeEdge)
    }

    fun selectNode(node: GraphNode?) {
        val selected = node?.takeIf { _nodes.contains(it) }
        if (_selectedNode.value == selected) {
            return
        }
        _selectedNode.set(selected)
        if (selected != null) {
            _selectedEdge.set(null)
        }
    }

    fun selectEdge(edge: GraphEdge?) {
        val selected = edge?.takeIf { _edges.contains(it) }
        if (_selectedEdge.value == selected) {
            return
        }
        _selectedEdge.set(selected)
        if (selected != null) {
            _selectedNode.set(null)
        }
    }

    fun clearSelection() {
        _selectedNode.set(null)
        _selectedEdge.set(null)
    }

    fun beginConnectionFrom(node: GraphNode) {
        if (_nodes.contains(node)) {
            _connectingFrom.set(node)
        }
    }

    fun cancelConnection() {
        _connectingFrom.set(null)
    }

    fun connect(
        source: GraphNode,
        target: GraphNode,
        label: String? = null,
        metadata: Map<String, String> = emptyMap(),
    ): GraphEdge? {
        if (!_nodes.contains(source) || !_nodes.contains(target)) {
            return null
        }
        if (!allowSelfEdges && source == target) {
            return null
        }
        return addEdge(sourceId = source.id, targetId = target.id, label = label, metadata = metadata).also {
            _connectingFrom.set(null)
        }
    }

    fun connectById(
        targetId: String,
        label: String? = null,
        metadata: Map<String, String> = emptyMap(),
    ): GraphEdge? {
        val source = connectingFrom ?: return null
        val target = nodeOf(targetId) ?: return null
        return connect(source, target, label, metadata)
    }

    fun deleteSelected(): Boolean {
        val edge = selectedEdge
        if (edge != null) {
            removeEdge(edge)
            return true
        }

        val node = selectedNode
        if (node != null) {
            removeNode(node)
            return true
        }
        return false
    }

    fun clear() {
        _nodes.clear()
        _edges.clear()
        clearSelection()
        cancelConnection()
        nodeSequence = 0
        edgeSequence = 0
    }

    fun onNodeCreated(handler: (GraphNode) -> Unit) {
        nodeCreatedHandlers += handler
    }

    fun onEdgeCreated(handler: (GraphEdge) -> Unit) {
        edgeCreatedHandlers += handler
    }

    fun onSelectedNodeChanged(handler: (GraphNode?) -> Unit) {
        selectedNodeProperty.addListener { _, _, node -> handler(node) }
    }

    fun onSelectedEdgeChanged(handler: (GraphEdge?) -> Unit) {
        selectedEdgeProperty.addListener { _, _, edge -> handler(edge) }
    }

    private fun nextNodeId(requested: String): String {
        val base = if (requested.isBlank()) "node-${++nodeSequence}" else requested
        return ensureUnique(base) { candidate -> nodeOf(candidate) != null }
    }

    private fun nextEdgeId(requested: String): String {
        val base = if (requested.isBlank()) "edge-${++edgeSequence}" else requested
        return ensureUnique(base) { candidate -> edgeOf(candidate) != null }
    }

    private fun ensureUnique(
        base: String,
        exists: (String) -> Boolean,
    ): String {
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
}
