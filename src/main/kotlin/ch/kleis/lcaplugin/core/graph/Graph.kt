package ch.kleis.lcaplugin.core.graph

import ch.kleis.lcaplugin.core.graph.GraphLinkType.BIOSPHERE_EXCHANGE
import ch.kleis.lcaplugin.core.graph.GraphLinkType.TECHNOSPHERE_EXCHANGE
import ch.kleis.lcaplugin.core.graph.GraphNodeType.*
import ch.kleis.lcaplugin.core.lang.value.BioExchangeValue
import ch.kleis.lcaplugin.core.lang.value.TechnoExchangeValue
import kotlinx.serialization.Serializable

/**
 * The different types of nodes. Used for markup purposes.
 * @property PROCESS A process - consumes and produces products and substances
 * @property PRODUCT A product - is exchanged by processes in technosphere exchanges
 * @property SUBSTANCE A substance - is consumed or produced by a process in a biosphere exchange
 */
@Serializable
enum class GraphNodeType {
    PROCESS, PRODUCT, SUBSTANCE
}

/**
 * The different types of links. Used for markup purposes.
 * @property TECHNOSPHERE_EXCHANGE An exchange of products in the technosphere
 * @property BIOSPHERE_EXCHANGE An exchange of substances in the biosphere
 */
@Serializable
enum class GraphLinkType {
    TECHNOSPHERE_EXCHANGE, BIOSPHERE_EXCHANGE
}

/**
 * Represents a node in the graph.
 *
 * @property key `key` is an id unique wrt the graph
 * @property type `type` is used for markup purposes
 * @property name `name` is the pretty-printed name
 */
@Serializable
data class GraphNode(
    val key: String, val type: GraphNodeType, val name: String
) {
    constructor(processName: String) : this(
        "PROCESS_$processName", PROCESS, processName
    )

    constructor(exchange: TechnoExchangeValue) : this(
        "PRODUCT_${exchange.product.name}", PRODUCT, exchange.product.name
    )

    constructor(exchange: BioExchangeValue) : this(
        "SUBSTANCE_${exchange.substance.name}", SUBSTANCE, exchange.substance.name
    )
}

/**
 * A link between two LCA nodes, representing an exchange in the Technosphere xor an exchange in the Biosphere.
 *
 * @property source The key of the node from which this link originates
 * @property target The key of the node to which this link points
 * @property type Exchange with the biosphere or with the technosphere
 * @property text A free text displayed on the list
 */
@Serializable
data class GraphLink(
        val source: String, val target: String, val type: GraphLinkType, val text: String
) {
    constructor(isInput: Boolean, processKey: String, exchange: TechnoExchangeValue) : this(
        if (isInput) "PRODUCT_${exchange.product.name}" else processKey,
        if (isInput) processKey else "PRODUCT_${exchange.product.name}",
        TECHNOSPHERE_EXCHANGE,
        exchange.quantity.toString()
    )

    constructor(processKey: String, exchange: BioExchangeValue) : this(
        processKey, "SUBSTANCE_${exchange.substance.name}", BIOSPHERE_EXCHANGE, exchange.quantity.toString()
    )
}

/**
 * A very simple, JSON serializable type description of an LCA directed process graph, consisting of a set of nodes and
 * a set of links between said nodes.
 */
@Serializable
data class Graph(val nodes: Set<GraphNode>, val links: Set<GraphLink>) {
    companion object {
        fun empty() = Graph(setOf(), setOf())
    }

    // Assuming we are going to kill the optics library
    fun addNode(node: GraphNode) = Graph(this.nodes + node, this.links)

    fun addLink(link: GraphLink) = Graph(this.nodes, this.links + link)

    fun merge(other: Graph): Graph = Graph(this.nodes union other.nodes, this.links union other.links)

    fun merge(vararg others: Graph): Graph = others.fold(this) { g0, g1 -> g0.merge(g1) }
}
