package ch.kleis.lcaplugin.core.lang

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.Merge
import ch.kleis.lcaplugin.core.lang.expression.optics.everyProcessTemplateInTemplateExpression

data class SymbolTable(
    val products: Register<LcaUnconstrainedProductExpression> = Register.empty(),
    val substances: Register<LcaSubstanceExpression> = Register.empty(),
    val indicators: Register<LcaIndicatorExpression> = Register.empty(),
    val quantities: Register<QuantityExpression> = Register.empty(),
    val units: Register<UnitExpression> = Register.empty(),
    val processTemplates: Register<ProcessTemplateExpression> = Register.empty(),
    val substanceCharacterizations: Register<ESubstanceCharacterization> = Register.empty(),
) {
    private val templatesIndexedByProductName: Index<ProcessTemplateExpression> = Index(processTemplates, Merge(
            listOf(
                everyProcessTemplateInTemplateExpression compose EProcessTemplate.body,
                ProcessTemplateExpression.eProcessFinal.expression,
            )
        ) compose
            EProcess.products compose
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

    fun getTemplate(name: String): ProcessTemplateExpression? {
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

    fun getTemplateFromProductName(name: String): ProcessTemplateExpression? {
        return templatesIndexedByProductName[name]
    }

    override fun toString(): String {
        return "[symbolTable]"
    }
}

