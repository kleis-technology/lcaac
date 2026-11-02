package ch.kleis.lcaac.cli.explain

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EUnitAlias
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations

enum class Origin {
    PARAMETER, VARIABLE, LABEL, ROW, GLOBAL_PARAMETER, GLOBAL_VARIABLE, UNIT;

    fun isGlobal() = this == GLOBAL_PARAMETER || this == GLOBAL_VARIABLE
}

/**
 * The names visible at some point of a process, where each name comes from,
 * and a reducer that evaluates expressions exactly like the evaluator does.
 */
class Scope private constructor(
    private val register: DataRegister<BasicNumber>,
    private val origins: Map<String, Origin>,
    private val dataSources: DataSourceRegister<BasicNumber>,
    private val sourceOps: DataSourceOperations<BasicNumber>,
) {
    val reducer = DataExpressionReducer(register, dataSources, BasicOperations, sourceOps)

    companion object {
        fun global(symbolTable: SymbolTable<BasicNumber>, sourceOps: DataSourceOperations<BasicNumber>): Scope {
            val origins = HashMap<String, Origin>()
            symbolTable.globalVariables.toMap().forEach { (key, expression) ->
                origins[key.name] =
                    if (expression is EUnitLiteral || expression is EUnitAlias) Origin.UNIT
                    else Origin.GLOBAL_VARIABLE
            }
            symbolTable.globalParameters.getKeys().forEach { origins[it.name] = Origin.GLOBAL_PARAMETER }
            return Scope(symbolTable.data, origins, symbolTable.dataSources, sourceOps)
        }
    }

    fun originOf(name: String): Origin? = origins[name]

    fun push(entries: Map<String, DataExpression<BasicNumber>>, origin: Origin): Scope {
        if (entries.isEmpty()) return this
        return Scope(
            register.plus(entries.mapKeys { DataKey(it.key) }),
            origins + entries.keys.associateWith { origin },
            dataSources,
            sourceOps,
        )
    }
}
