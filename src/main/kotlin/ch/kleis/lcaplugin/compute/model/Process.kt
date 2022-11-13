package ch.kleis.lcaplugin.compute.model

import ch.kleis.lcaplugin.compute.traits.HasUniqueId

data class Process(
    val name: String,
    val products: List<IntermediaryExchange<*>>,
    val inputs: List<IntermediaryExchange<*>>,
    val emissions: List<ElementaryExchange<*>>,
    val resources: List<ElementaryExchange<*>>
) : HasUniqueId {
    override fun getUniqueId(): String {
        return name
    }
}

