package ch.kleis.lcaplugin.imports.ecospold.lcia.model


data class FlowData(
    val intermediateExchanges: List<IntermediateExchange> = ArrayList(),
    val impactIndicators: List<ImpactIndicator>
)