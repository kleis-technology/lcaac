package ch.kleis.lcaac.core.graph

import kotlinx.serialization.Serializable

@Serializable
data class GraphNode(
    val key: String, val name: String
)

@Serializable
data class GraphLink(
    val source: String, val target: String, val value: Double
)

@Serializable
data class Graph(val nodes: Set<GraphNode>, val links: Set<GraphLink>) {
    companion object {
        fun empty() = Graph(setOf(), setOf())
    }

    // Assuming we are going to kill the optics library
    fun addNode(node: GraphNode) = Graph(this.nodes + node, this.links)

    fun addNode(vararg nodes: GraphNode) = nodes.fold(this) { g, n -> g.addNode(n) }

    fun addLink(link: GraphLink) = Graph(this.nodes, this.links + link)

    fun addLink(vararg links: GraphLink) = links.fold(this) { g, l -> g.addLink(l) }

    fun merge(other: Graph): Graph = Graph(this.nodes union other.nodes, this.links union other.links)

    fun merge(vararg others: Graph): Graph = others.fold(this) { g0, g1 -> g0.merge(g1) }
}
