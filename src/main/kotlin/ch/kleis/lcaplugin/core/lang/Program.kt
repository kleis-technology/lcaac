package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.expression.Expression

data class Program(
    val entryPoint: Expression,
    val symbolTable: SymbolTable,
)
