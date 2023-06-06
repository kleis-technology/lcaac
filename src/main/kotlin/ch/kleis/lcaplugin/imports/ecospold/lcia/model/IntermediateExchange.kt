package ch.kleis.lcaplugin.imports.ecospold.lcia.model


data class Property(
    val name: String,
    val amount: Double,
    val unit: String,
    val isDefiningValue: String?,
    val isCalculatedAmount: String?
)

class IntermediateExchange private constructor(
    val amount: Double,
    val name: String?,
    val unit: String,
    val synonyms: List<String>,
    val uncertainty: Uncertainty?,
    val outputGroup: Int? = null,
    val classifications: List<Classification> = ArrayList(),
    val properties: List<Property> = ArrayList()
) {
    data class Builder(
        var amount: Double? = null,
        var name: String? = null,
        var unit: String? = null,
        var synonyms: List<String> = ArrayList(),
        var uncertainty: Uncertainty? = null,
        var outputGroup: Int? = null,
        var classifications: List<Classification> = ArrayList(),
        var properties: List<Property> = ArrayList()

    ) {
        fun amount(amount: Double) = apply { this.amount = amount }
        fun name(name: String) = apply { this.name = name }
        fun unit(unit: String) = apply { this.unit = unit }
        fun synonyms(synonyms: List<String>) = apply { this.synonyms = synonyms }
        fun uncertainty(uncertainty: Uncertainty?) = apply { this.uncertainty = uncertainty }
        fun outputGroup(outputGroup: Int) = apply { this.outputGroup = outputGroup }
        fun classifications(classifications: List<Classification>) = apply { this.classifications = classifications }
        fun properties(properties: List<Property>) = apply { this.properties = properties }

        fun build(): IntermediateExchange =
            IntermediateExchange(
                amount!!,
                name,
                unit!!,
                synonyms,
                uncertainty,
                outputGroup,
                classifications,
                properties
            )
    }
}