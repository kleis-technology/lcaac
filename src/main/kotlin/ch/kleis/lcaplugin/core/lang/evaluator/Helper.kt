package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.everyQuantityRef
import ch.kleis.lcaplugin.core.lang.expression.optics.everyQuantityRefInProcess
import ch.kleis.lcaplugin.core.lang.expression.optics.everyStringRefInEProcess

class Helper {
    fun substitute(binder: String, value: DataExpression, body: EProcess): EProcess {
        return when (value) {
            is QuantityExpression -> everyQuantityRefInProcess.modify(body) {
                if (it.name == binder) value else it
            }

            is StringExpression -> everyStringRefInEProcess.modify(body) {
                if (it.name == binder) value else it
            }
        }
    }

    fun allRequiredRefs(expression: Expression): Set<String> {
        val allRefs = everyQuantityRef compose EQuantityRef.name
        return allRefs.getAll(expression).toSet()
    }
}
