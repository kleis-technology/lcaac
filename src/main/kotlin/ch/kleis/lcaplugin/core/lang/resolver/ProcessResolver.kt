package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplateRef
import ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression

class ProcessResolver(
    private val symbolTable: SymbolTable
) {
    fun resolve(processName: String): ProcessTemplateExpression? {
        return recursiveResolve(emptyList(), processName)
    }

    private tailrec fun recursiveResolve(visited: List<String>, processName: String): ProcessTemplateExpression? {
        if (visited.contains(processName)) {
            throw EvaluatorException("cycle detected: $visited")
        }
        return when(val p = symbolTable.processTemplates[processName]) {
            is EProcessTemplateRef -> recursiveResolve(visited.plus(processName), p.name)
            else -> p
        }
    }

    fun resolveByProductName(productName: String): ProcessTemplateExpression? {
        return symbolTable.getTemplateFromProductName(productName)
    }

}
