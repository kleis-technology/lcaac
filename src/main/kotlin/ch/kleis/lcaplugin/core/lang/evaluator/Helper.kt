package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.*

class Helper {
    fun substitute(binder: String, value: QuantityExpression, body: LcaProcessExpression): LcaProcessExpression {
        return everyQuantityRefInProcessExpression.modify(body) {
            if (it.name == binder) value else it
        }
    }

    fun allRequiredRefs(expression: Expression): Set<String> {
        val allRefs : Every<Expression, String> = Merge(listOf(
            everyRequiredProductRef compose EProductRef.name,
            everyQuantityRef compose EQuantityRef.name,
            everyTemplateRef compose ETemplateRef.name,
            everyUnitRef compose EUnitRef.name
        ))
        return allRefs.getAll(expression).toSet()
    }
}
