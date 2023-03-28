package ch.kleis.lcaplugin.core.lang.resolver

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.Merge
import ch.kleis.lcaplugin.core.lang.expression.optics.everyProcessTemplateInTemplateExpression

class ProcessResolver(
    private val symbolTable: SymbolTable
) {
    fun resolve(processName: String): TemplateExpression? {
        return recursiveResolve(emptyList(), processName)
    }

    private fun recursiveResolve(visited: List<String>, processName: String): TemplateExpression? {
        if (visited.contains(processName)) {
            throw EvaluatorException("cycle detected: $visited")
        }
        return when(val p = symbolTable.processTemplates[processName]) {
            is ETemplateRef -> recursiveResolve(visited.plus(processName), p.name)
            else -> p
        }
    }

    fun resolveByProductName(productName: String): Set<Pair<String, TemplateExpression>> {
        val optics = Merge(
            listOf(
                everyProcessTemplateInTemplateExpression compose EProcessTemplate.body,
                TemplateExpression.eProcessFinal.expression,
            )
        ) compose
                LcaProcessExpression.eProcess.products compose
                Every.list() compose
                ETechnoExchange.product.eConstrainedProduct.product compose
                Merge(
                    listOf(
                        LcaUnconstrainedProductExpression.eProduct.name,
                        LcaUnconstrainedProductExpression.eProductRef.name,
                    )
                )
        return symbolTable.processTemplates.entries
            .filter {
                optics.getAll(it.value).contains(productName)
            }
            .map { it.key to it.value }
            .toSet()
    }
}
