package ch.kleis.lcaac.core.lang.evaluator.reducer

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.EUnitAlias

class InfiniteUnitLoopChecker<Q> {
    private val unitAliasRegister = mutableSetOf<EUnitAlias<Q>>()

    fun check(expression: EUnitAlias<Q>) {
        if (unitAliasRegister.contains(expression)) {
            throw EvaluatorException("Recursive dependency for unit ${expression.symbol}")
        }
        unitAliasRegister.add(expression)
    }

    fun clearTraceAlias() {
        unitAliasRegister.clear()
    }
}
