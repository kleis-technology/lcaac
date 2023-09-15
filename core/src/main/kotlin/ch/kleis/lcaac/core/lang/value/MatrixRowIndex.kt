package ch.kleis.lcaac.core.lang.value

import ch.kleis.lcaac.core.HasUID

sealed interface MatrixRowIndex<Q> : Value<Q>, HasUID

sealed interface HasImpactList<Q> {
    val impacts: List<ImpactValue<Q>>
}

data class ProcessValue<Q>(
    val name: String,
    val labels: Map<String, StringValue<Q>> = emptyMap(),
    val products: List<TechnoExchangeValue<Q>> = emptyList(),
    val inputs: List<TechnoExchangeValue<Q>> = emptyList(),
    val biosphere: List<BioExchangeValue<Q>> = emptyList(),
    override val impacts: List<ImpactValue<Q>> = emptyList(),
) : Value<Q>, HasImpactList<Q>, MatrixRowIndex<Q> {
    val outputExchangesByProduct = products
        .associateBy { it.product }
}

data class SubstanceCharacterizationValue<Q>(
    val referenceExchange: BioExchangeValue<Q>,
    override val impacts: List<ImpactValue<Q>> = emptyList(),
) : Value<Q>, HasImpactList<Q>, MatrixRowIndex<Q>
