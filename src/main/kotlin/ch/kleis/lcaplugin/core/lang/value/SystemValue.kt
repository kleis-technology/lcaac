package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.HasUID


@optics
data class SystemValue(
    val processes: List<ProcessValue>,
    val substanceCharacterizations: List<SubstanceCharacterizationValue>,
) : Value, HasUID {
    companion object
}

@optics
data class CharacterizationFactorValue(
    val output: ExchangeValue,
    val input: ExchangeValue,
) : Value {
    companion object
}
