package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.everyDataRef
import ch.kleis.lcaplugin.core.lang.expression.optics.everyDataRefInProcess

class Helper {
    fun substitute(binder: String, value: DataExpression, body: EProcess): EProcess {
        return everyDataRefInProcess.modify(body) { if (it.name == binder) value else it }
    }

    fun allRequiredRefs(expression: Expression): Set<String> {
        val allRefs = everyDataRef compose EDataRef.name
        return allRefs.getAll(expression).toSet()
    }
}
