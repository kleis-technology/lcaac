package ch.kleis.lcaac.core.lang.value

import ch.kleis.lcaac.core.HasUID


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

