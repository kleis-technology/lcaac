package ch.kleis.lcaplugin.imports.ecospold.model


data class Property(
    val name: String,
    val amount: Double,
    val unit: String,
    val isDefiningValue: String?,
    val isCalculatedAmount: String?
)

data class IntermediateExchange(
    val amount: Double,
    val name: String?,
    val unit: String,
    val synonyms: List<String> = emptyList(),
    val uncertainty: Uncertainty?,
    val outputGroup: Int? = null,
    val classifications: List<Classification> = emptyList(),
    val properties: List<Property> = emptyList(),
)