package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.*

class Helper {
    fun substitute(binder: String, value: QuantityExpression, body: EProcess): EProcess {
        return everyQuantityRefInProcess.modify(body) {
            if (it.name == binder) value else it
        }
    }

    fun allRequiredRefs(expression: Expression): Set<String> {
        val allRefs: Every<Expression, String> = Merge(
            listOf(
                everyQuantityRef compose EQuantityRef.name,
                everyTemplateRef compose EProcessTemplateRef.name,
                everyUnitRef compose EUnitRef.name
            )
        )
        return allRefs.getAll(expression).toSet()
    }
}
