package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.HasUID


data class SystemValue<Q>(
    val processes: Set<ProcessValue<Q>> = emptySet(),
    val substanceCharacterizations: Set<SubstanceCharacterizationValue<Q>> = emptySet(),
) : Value<Q>, HasUID {

    val productToProcessMap: Map<ProductValue<Q>, ProcessValue<Q>> =
        processes.flatMap { process -> process.products.map { it.product to process } }.toMap()
    val substanceToSubstanceCharacterizationMap: Map<SubstanceValue<Q>, SubstanceCharacterizationValue<Q>> =
        substanceCharacterizations
            .associateBy { characterization -> characterization.referenceExchange.substance }

}

data class CharacterizationFactorValue<Q>(
    val output: ExchangeValue<Q>,
    val input: ExchangeValue<Q>,
) : Value<Q>
