package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics

@optics
sealed interface ProcessTemplateExpression : Expression {
    companion object
}

@optics
data class EProcessTemplate(
    val params: Map<String, DataExpression>,
    val locals: Map<String, DataExpression>,
    val body: EProcess,
) : ProcessTemplateExpression {
    companion object
}

@optics
data class EProcessTemplateApplication(
    val template: EProcessTemplate,
    val arguments: Map<String, DataExpression>
) : ProcessTemplateExpression {
    companion object
}

@optics
data class EProcessFinal(val expression: EProcess) : ProcessTemplateExpression {
    companion object
}


