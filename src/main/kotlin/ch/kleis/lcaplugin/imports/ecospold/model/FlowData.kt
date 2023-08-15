package ch.kleis.lcaplugin.imports.ecospold.model


data class FlowData(
    val intermediateExchanges: Sequence<IntermediateExchange> = emptySequence(),
    val impactIndicators: Sequence<ImpactIndicator> = emptySequence(),
    val elementaryExchanges: Sequence<ElementaryExchange> = emptySequence(),
)