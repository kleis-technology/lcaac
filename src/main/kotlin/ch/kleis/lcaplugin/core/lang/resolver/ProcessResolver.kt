package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplate
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplateRef
import ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression

class ProcessResolver(
    private val symbolTable: SymbolTable
) {
    fun resolve(processName: String): EProcessTemplate? {
        return symbolTable.getTemplate(processName)
    }

    fun resolveByProductName(productName: String): EProcessTemplate? {
        return symbolTable.getTemplateFromProductName(productName)
    }

}
