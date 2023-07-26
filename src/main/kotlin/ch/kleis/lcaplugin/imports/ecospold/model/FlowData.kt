package ch.kleis.lcaplugin.imports.ecospold.model


data class FlowData(
        val intermediateExchanges: List<IntermediateExchange> = ArrayList(),
        val impactIndicators: List<ImpactIndicator>,
        val elementaryExchanges: Sequence<ElementaryExchange>,
)