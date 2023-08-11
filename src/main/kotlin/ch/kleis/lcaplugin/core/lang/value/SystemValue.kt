package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.HasUID


data class SystemValue(
    val processes: Set<ProcessValue> = emptySet(),
    val substanceCharacterizations: Set<SubstanceCharacterizationValue> = emptySet(),
) : Value, HasUID {

    val productToProcessMap: Map<ProductValue, ProcessValue> =
        processes.flatMap { process -> process.products.map { it.product to process } }.toMap()

}

data class CharacterizationFactorValue(
    val output: ExchangeValue,
    val input: ExchangeValue,
) : Value
