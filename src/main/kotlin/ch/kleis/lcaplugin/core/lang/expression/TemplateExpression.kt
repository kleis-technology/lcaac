package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.*

@optics
sealed interface TemplateExpression : Expression {
    companion object
}

@optics
data class EProcessTemplate(
    val params: Map<String, QuantityExpression>,
    val locals: Map<String, QuantityExpression>,
    val body: LcaProcessExpression,
) : TemplateExpression {
    companion object
}

@optics
data class EInstance(
    val template: TemplateExpression,
    val arguments: Map<String, QuantityExpression>
) : TemplateExpression {
    companion object
}

@optics
data class ETemplateRef(val name: String) : TemplateExpression, RefExpression {
    override fun name(): String {
        return name
    }

    companion object
}

@optics
data class EProcessFinal(val expression: LcaProcessExpression) : TemplateExpression {
    companion object
}


