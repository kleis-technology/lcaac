package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics

@optics
sealed interface ProcessTemplateExpression : Expression {
    companion object
}

@optics
data class EProcessTemplate(
    val params: Map<String, QuantityExpression>,
    val locals: Map<String, QuantityExpression>,
    val body: LcaProcessExpression,
) : ProcessTemplateExpression {
    companion object
}

@optics
data class EProcessTemplateApplication(
    val template: ProcessTemplateExpression,
    val arguments: Map<String, QuantityExpression>
) : ProcessTemplateExpression {
    companion object
}

@optics
data class EProcessTemplateRef(val name: String) : ProcessTemplateExpression, RefExpression {
    override fun name(): String {
        return name
    }

    companion object
}

@optics
data class EProcessFinal(val expression: LcaProcessExpression) : ProcessTemplateExpression {
    companion object
}


