package ch.kleis.lcaac.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*

class CompleteDefaultArguments<Q>(
    private val symbolTable: SymbolTable<Q>,
) {
    private val everyInputProduct =
        EProcessTemplateApplication.template<Q>().body().inputs() compose
            Every.list() compose
            ETechnoExchange.product()

    fun apply(expression: EProcessTemplateApplication<Q>): EProcessTemplateApplication<Q> {
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

    private fun evalLabel(entry: Map.Entry<String, DataExpression<Q>>): String {
        val key = entry.key
        return when (val expression = entry.value) {
            is EStringLiteral -> expression.value
            else -> throw EvaluatorException("$key = $expression is not a valid label value")
        }
    }
}
