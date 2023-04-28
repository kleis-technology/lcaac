package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.expression.*

data class SymbolTable(
    val products: Register<LcaUnconstrainedProductExpression> = Register.empty(),
    val substances: Register<LcaSubstanceExpression> = Register.empty(),
    val indicators: Register<LcaIndicatorExpression> = Register.empty(),
    val quantities: Register<QuantityExpression> = Register.empty(),
    val units: Register<UnitExpression> = Register.empty(),
    val processTemplates: Register<ProcessTemplateExpression> = Register.empty(),
    private val templatesIndexedByProduct: Index<ProcessTemplateExpression> = Index.empty(),
    val substanceCharacterizations: Register<ESubstanceCharacterization> = Register.empty(),
) {
    companion object {
        fun empty() = SymbolTable()
    }

    fun getTemplate(name: String): ProcessTemplateExpression? {
        return processTemplates[name]
    }

    fun getUnit(name: String): UnitExpression? {
        return units[name]
    }

    fun getQuantity(name: String): QuantityExpression? {
        return quantities[name]
    }

    fun plus(map: Map<String, QuantityExpression>): SymbolTable {
        return SymbolTable(
            products,
            substances,
            indicators,
            quantities.plus(map),
            units,
            processTemplates,
            templatesIndexedByProduct,
            substanceCharacterizations,
        )
    }

    fun getSubstanceCharacterization(name: String): ESubstanceCharacterization? {
        return substanceCharacterizations[name]
    }

    fun getSubstance(name: String): LcaSubstanceExpression? {
        return substances[name]
    }

    fun getTemplateFromProductName(name: String): ProcessTemplateExpression? {
        return templatesIndexedByProduct[name]
    }

    override fun toString(): String {
        return "[symbolTable]"
    }
}

