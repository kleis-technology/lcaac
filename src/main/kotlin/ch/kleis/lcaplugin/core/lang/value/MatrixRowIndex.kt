package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.HasUID

sealed interface MatrixRowIndex : Value, HasUID

sealed interface HasImpactList {
    val impacts: List<ImpactValue>
}

data class ProcessValue(
    val name: String,
    val labels: Map<String, StringValue>,
    val products: List<TechnoExchangeValue>,
    val inputs: List<TechnoExchangeValue>,
    val biosphere: List<BioExchangeValue>,
    override val impacts: List<ImpactValue>
) : Value, HasImpactList, MatrixRowIndex

data class SubstanceCharacterizationValue(
    val referenceExchange: BioExchangeValue,
    override val impacts: List<ImpactValue>,
) : Value, HasImpactList, MatrixRowIndex
