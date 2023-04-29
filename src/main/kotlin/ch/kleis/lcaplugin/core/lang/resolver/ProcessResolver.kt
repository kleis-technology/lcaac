package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplate
import ch.kleis.lcaplugin.core.lang.expression.EProductSpec

class ProcessResolver(
    private val symbolTable: SymbolTable
) {
    fun resolve(spec: EProductSpec): EProcessTemplate? {
        return symbolTable.getTemplateFromProductName(spec.name)
    }

}
