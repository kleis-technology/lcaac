package ch.kleis.lcaplugin.core.lang.expression

sealed interface TemplateExpression : Expression

data class EProcessTemplate(
    val params: Map<String, QuantityExpression>,
    val locals: Map<String, QuantityExpression>,
    val body: LcaProcessExpression,
) : TemplateExpression

data class EInstance(
    val template: TemplateExpression,
    val arguments: Map<String, QuantityExpression>
) : TemplateExpression
data class ETemplateRef(val name: String): TemplateExpression, RefExpression {
    override fun name(): String {
        return name
    }
}

data class EProcessFinal(val expression: LcaProcessExpression) : TemplateExpression
