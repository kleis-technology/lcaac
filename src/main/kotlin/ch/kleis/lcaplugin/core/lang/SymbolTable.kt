package ch.kleis.lcaplugin.core.lang

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.Merge

data class SymbolTable(
    val products: Register<EProduct> = Register.empty(),
    val substances: Register<ESubstance> = Register.empty(),
    val indicators: Register<EIndicator> = Register.empty(),
    val quantities: Register<QuantityExpression> = Register.empty(),
    val units: Register<UnitExpression> = Register.empty(),
    val processTemplates: Register<EProcessTemplate> = Register.empty(),
    val substanceCharacterizations: Register<ESubstanceCharacterization> = Register.empty(),
) {
    private val templatesIndexedByProductName: Index<EProcessTemplate> = Index(
        processTemplates,
        EProcessTemplate.body.products compose
                Every.list() compose
                ETechnoExchange.product.product compose
                Merge(
                    listOf(
                        LcaUnconstrainedProductExpression.eProduct.name,
                        LcaUnconstrainedProductExpression.eProductRef.name,
                    )
                )
    )

    companion object {
        fun empty() = SymbolTable()
    }

    fun getTemplate(name: String): EProcessTemplate? {
        return processTemplates[name]
    }

    fun getUnit(name: String): UnitExpression? {
        return units[name]
    }

    fun getQuantity(name: String): QuantityExpression? {
        return quantities[name]
    }

    fun getSubstanceCharacterization(name: String): ESubstanceCharacterization? {
        return substanceCharacterizations[name]
    }

    fun getSubstance(name: String): LcaSubstanceExpression? {
        return substances[name]
    }

    fun getTemplateFromProductName(name: String): EProcessTemplate? {
        return templatesIndexedByProductName[name]
    }

    override fun toString(): String {
        return "[symbolTable]"
    }
}

