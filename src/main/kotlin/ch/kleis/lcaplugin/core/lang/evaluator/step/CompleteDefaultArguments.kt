package ch.kleis.lcaplugin.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*

class CompleteDefaultArguments(
    private val symbolTable: SymbolTable,
) {
    private val everyInputProduct =
        ProcessTemplateExpression.eProcessTemplateApplication.template.body.inputs compose
            Every.list() compose
            ETechnoExchange.product

    fun apply(expression: ProcessTemplateExpression): ProcessTemplateExpression {
        return when (expression) {
            is EProcessFinal -> expression
            is EProcessTemplate -> this.apply(EProcessTemplateApplication(expression, emptyMap()))
            is EProcessTemplateApplication -> everyInputProduct.modify(expression) {
                it.fromProcess?.let { ref ->
                    val process = symbolTable.getTemplate(ref.name)
                        ?: throw EvaluatorException("unknown process ${ref.name}")
                    val actualArguments = process.params.plus(ref.arguments)
                    it.copy(
                        fromProcess = it.fromProcess.copy(
                            arguments = actualArguments
                        )
                    )
                } ?: it
            }
        }
    }
}
