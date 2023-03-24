package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.compiler.Compiler
import ch.kleis.lcaplugin.core.lang.evaluator.linker.Linker
import ch.kleis.lcaplugin.core.lang.expression.TemplateExpression
import ch.kleis.lcaplugin.core.lang.value.SystemValue

class Evaluator(
    private val symbolTable: SymbolTable,
) {

    fun eval(expression: TemplateExpression): SystemValue {
        val compiler = Compiler(symbolTable)
        val systemObject = compiler.compile(expression)
        return Linker().link(systemObject)
    }

}

