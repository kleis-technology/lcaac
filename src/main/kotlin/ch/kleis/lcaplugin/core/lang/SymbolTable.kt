package ch.kleis.lcaplugin.core.lang

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.expression.*

data class SymbolTable(
    val substances: Register<ESubstanceSpec> = Register.empty(),
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
                ETechnoExchange.product compose
                EProductSpec.name
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

    fun getSubstance(name: String): ESubstanceSpec? {
        return substances[name]
    }

    fun getTemplateFromProductName(name: String): EProcessTemplate? {
        return templatesIndexedByProductName[name]
    }

    override fun toString(): String {
        return "[symbolTable]"
    }
}

