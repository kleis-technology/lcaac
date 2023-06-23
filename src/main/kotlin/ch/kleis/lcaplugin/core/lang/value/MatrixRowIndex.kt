package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.HasUID

sealed interface MatrixRowIndex : Value, HasUID

data class ProcessValue(
    val name: String,
    val labels: Map<String, StringValue>,
    val products: List<TechnoExchangeValue>,
    val inputs: List<TechnoExchangeValue>,
    val biosphere: List<BioExchangeValue>,
) : Value, MatrixRowIndex

data class SubstanceCharacterizationValue(
    val referenceExchange: BioExchangeValue,
    val impacts: List<ImpactValue>,
) : Value, MatrixRowIndex
