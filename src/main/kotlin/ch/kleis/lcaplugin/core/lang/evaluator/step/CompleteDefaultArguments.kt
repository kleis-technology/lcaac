package ch.kleis.lcaplugin.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*

class CompleteDefaultArguments(
    private val symbolTable: SymbolTable,
) {
    private val everyInputProduct =
        ProcessTemplateExpression.eProcessTemplateApplication.template.eProcessTemplate.body.inputs compose
                Every.list() compose
                ETechnoExchange.product

    fun apply(expression: ProcessTemplateExpression): ProcessTemplateExpression {
        return when (expression) {
            is EProcessFinal -> expression
            is EProcessTemplate -> this.apply(EProcessTemplateApplication(expression, emptyMap()))
            is EProcessTemplateApplication -> everyInputProduct.modify(expression) {
                it.fromProcessRef?.let { ref ->
                    val process = symbolTable.getTemplate(ref.ref)
                        ?: throw EvaluatorException("unknown process ${ref.ref}")
                    val actualArguments = process.params.plus(ref.arguments)
                    EProductSpec(
                        it.name,
                        it.referenceUnit,
                        FromProcessRef(
                            ref.ref,
                            actualArguments,
                        )
                    )
                } ?: it
            }
        }
    }
}
