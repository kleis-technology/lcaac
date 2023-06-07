package ch.kleis.lcaplugin.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*

class CompleteDefaultArguments(
    private val symbolTable: SymbolTable,
) {
    private val everyInputProduct =
        EProcessTemplateApplication.template.body.inputs compose
            Every.list() compose
            ETechnoExchange.product

    fun apply(expression: EProcessTemplateApplication): EProcessTemplateApplication {
        return everyInputProduct.modify(expression) {
            it.fromProcess?.let { ref ->
                val name = ref.name
                val matchLabels = ref.matchLabels.elements.mapValues { entry -> evalLabel(entry) }
                val process = symbolTable.getTemplate(name, matchLabels)
                    ?: throw EvaluatorException("unknown process $name$matchLabels")
                val actualArguments = process.params.plus(ref.arguments)
                it.copy(
                    fromProcess = it.fromProcess.copy(
                        matchLabels = MatchLabels(matchLabels.mapValues { entry -> EStringLiteral(entry.value) }),
                        arguments = actualArguments
                    )
                )
            } ?: it
        }
    }

    private fun evalLabel(entry: Map.Entry<String, DataExpression>): String {
        val key = entry.key
        return when (val expression = entry.value) {
            is EStringLiteral -> expression.value
            else -> throw EvaluatorException("$key = $expression is not a valid label value")
        }
    }
}
