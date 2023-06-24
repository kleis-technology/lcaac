package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.HasUID


data class SystemValue(
    val processes: Set<ProcessValue>,
    val substanceCharacterizations: Set<SubstanceCharacterizationValue>,
) : Value, HasUID

data class CharacterizationFactorValue(
    val output: ExchangeValue,
    val input: ExchangeValue,
) : Value
