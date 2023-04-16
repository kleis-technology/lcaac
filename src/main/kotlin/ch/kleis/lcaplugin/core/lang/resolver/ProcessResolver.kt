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

    fun resolveByProductName(productName: String): TemplateExpression? {
        return symbolTable.getTemplateFromProductName(productName)
    }

}
