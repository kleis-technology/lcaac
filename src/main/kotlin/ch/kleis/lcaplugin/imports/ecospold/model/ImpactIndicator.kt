package ch.kleis.lcaplugin.imports.ecospold.model

data class ImpactIndicator(
    val methodName: String,
    val categoryName: String,
    val name: String,
    val amount: Double,
    val unitName: String
)
