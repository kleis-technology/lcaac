package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.*

class Beta {
    fun substitute(binder: String, value: QuantityExpression, body: LcaProcessExpression): LcaProcessExpression {
        return everyQuantityRefInProcessExpression.modify(body) {
            if (it.name == binder) value else it
        }
    }
}

class Helper {
    fun unboundedReferences(expression: Expression): Set<String> {
        val allRefs : Every<Expression, String> = Merge(listOf(
            everyIndicatorRef compose EIndicatorRef.name,
            everyProductRef compose EProductRef.name,
            everyQuantityRef compose EQuantityRef.name,
            everySubstanceRef compose ESubstanceRef.name,
            everyTemplateRef compose ETemplateRef.name,
            everyUnitRef compose EUnitRef.name
        ))
        return allRefs.getAll(expression).toSet()
    }
}
