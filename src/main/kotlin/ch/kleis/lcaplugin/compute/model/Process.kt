package ch.kleis.lcaplugin.compute.model

data class Process(
    val name: String,
    val products: List<IntermediaryExchange<*>>,
    val inputs: List<IntermediaryExchange<*>>,
    val emissions: List<ElementaryExchange<*>>,
    val resources: List<ElementaryExchange<*>>
)

