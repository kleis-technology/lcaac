package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.expression.optics.*

class Helper<Q> {
    fun substitute(binder: String, value: DataExpression<Q>, body: EProcess<Q>): EProcess<Q> {
        return everyDataRefInProcess<Q>().modify(body) { if (it.name == binder) value else it }
    }

    fun allRequiredRefs(expression: LcaExpression<Q>): Set<String> {
        val allRefs = everyDataRefInLcaExpression<Q>() compose EDataRef.name()
        return allRefs.getAll(expression).toSet()
    }
}
