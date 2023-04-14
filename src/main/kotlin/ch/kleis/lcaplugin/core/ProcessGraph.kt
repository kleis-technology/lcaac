package ch.kleis.lcaplugin.core

import kotlinx.serialization.Serializable

/**
 * The different types of nodes. Used for markup purposes.
 */
@Serializable
enum class ProcessGraphNodeType {
    PROCESS,    /** A process - consumes and produces products and substances */
    PRODUCT,    /** A product - is exchanged by processes in technosphere exchanges */
    SUBSTANCE   /** A substance - is consumed or produced by a process in a biosphere exchange */
}

/**
 * The different types of links. Used for markup purposes.
 */
@Serializable
enum class ProcessGraphLinkType {
    TECHNOSPHERE_EXCHANGE,  /** An exchange of products in the technosphere */
    BIOSPHERE_EXCHANGE      /** An exchange of substances in the biosphere */
}

/**
 * A node in our LCA visual graph representation.
 *
 * @param key A unique identifier for this node.
 * @param type Type of node represented. Used for markup purposes.
 * @param name The displayed name of this node.
 */
@Serializable
data class ProcessGraphNode(val key: String, val type: ProcessGraphNodeType, val name: String)

/**
 * A link between two LCA nodes, representing an exchange in the Technosphere xor an exchange in the Biosphere.
 *
 * @param from The `GraphNode.key` of the node originating the link.
 * @param to The `GraphNode.key` of the node receiving the link.
 * @param type The type of exchange taking place, for markup purposes.
 * @param text Text to be displayed on the link in the graph.
 */
@Serializable
data class ProcessGraphLink(val from: String, val to: String, val type: ProcessGraphLinkType, val text: String)

/**
 * A very simple, JSON serializable type description of an LCA directed process graph, consisting of a set of nodes and
 * a set of links between said nodes.
 */
@Serializable
data class ProcessGraph(val nodes: Set<ProcessGraphNode>, val links: Set<ProcessGraphLink>) {
    companion object {
        fun empty() = ProcessGraph(setOf(), setOf())
    }

    // Assuming we are going to kill the optics library
    fun addNode(node: ProcessGraphNode) =
            ProcessGraph(this.nodes + node, this.links)

    fun addLink(link: ProcessGraphLink) =
            ProcessGraph(this.nodes, this.links + link)

    fun merge(other: ProcessGraph): ProcessGraph =
            ProcessGraph(this.nodes union other.nodes, this.links union other.links)

    fun merge(vararg others: ProcessGraph): ProcessGraph =
            others.fold(this) { g0, g1 -> g0.merge(g1) }
}
