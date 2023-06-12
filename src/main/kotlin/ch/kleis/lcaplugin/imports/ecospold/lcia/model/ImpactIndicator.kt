package ch.kleis.lcaplugin.imports.ecospold.lcia.model

class ImpactIndicator(
    val methodName: String,
    val categoryName: String,
    val name: String,
    val amount: Double,
    val unitName: String
) {
    data class Builder(
        var methodName: String? = null,
        var categoryName: String? = null,
        var name: String? = null,
        var amount: Double? = null,
        var unitName: String? = null
    ) {
        fun methodName(methodName: String) = apply { this.methodName = methodName }
        fun categoryName(categoryName: String) = apply { this.categoryName = categoryName }
        fun name(name: String) = apply { this.name = name }
        fun amount(amount: Double) = apply { this.amount = amount }
        fun unitName(unitName: String) = apply { this.unitName = unitName }
        fun build(): ImpactIndicator {
            return ImpactIndicator(methodName!!, categoryName!!, name!!, amount!!, unitName!!)
        }
    }

}
