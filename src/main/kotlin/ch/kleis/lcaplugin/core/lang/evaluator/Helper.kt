package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.everyDataRef
import ch.kleis.lcaplugin.core.lang.expression.optics.everyDataRefInProcess

class Helper<Q> {
    fun substitute(binder: String, value: DataExpression<Q>, body: EProcess<Q>): EProcess<Q> {
        return everyDataRefInProcess<Q>().modify(body) { if (it.name == binder) value else it }
    }

    fun allRequiredRefs(expression: Expression<Q>): Set<String> {
        val allRefs = everyDataRef<Q>() compose EDataRef.name<Q>()
        return allRefs.getAll(expression).toSet()
    }
}
