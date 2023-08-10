package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.HasUID

sealed interface MatrixRowIndex : Value, HasUID

sealed interface HasImpactList {
    val impacts: List<ImpactValue>
}

data class ProcessValue(
    val name: String,
    val labels: Map<String, StringValue> = emptyMap(),
    val products: List<TechnoExchangeValue> = emptyList(),
    val inputs: List<TechnoExchangeValue> = emptyList(),
    val biosphere: List<BioExchangeValue> = emptyList(),
    override val impacts: List<ImpactValue> = emptyList(),
) : Value, HasImpactList, MatrixRowIndex

data class SubstanceCharacterizationValue(
    val referenceExchange: BioExchangeValue,
    override val impacts: List<ImpactValue> = emptyList(),
) : Value, HasImpactList, MatrixRowIndex
