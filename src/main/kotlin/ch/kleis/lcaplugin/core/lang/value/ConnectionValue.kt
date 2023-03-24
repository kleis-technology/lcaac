package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.HasUID

sealed interface ConnectionValue : Value, HasUID

@optics
data class ProcessValue(
    val name: String,
    val products: List<TechnoExchangeValue>,
    val inputs: List<TechnoExchangeValue>,
    val biosphere: List<BioExchangeValue>,
) : Value, ConnectionValue {
    companion object
}

@optics
data class SubstanceCharacterizationValue(
    val referenceExchange: BioExchangeValue,
    val impacts: List<ImpactValue>,
) : Value, ConnectionValue {
    companion object
}
