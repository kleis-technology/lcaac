package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.*

val everyProcessTemplateInTemplateExpression = object : PEvery<TemplateExpression, TemplateExpression, EProcessTemplate, TemplateExpression> {
    override fun <R> foldMap(M: Monoid<R>, source: TemplateExpression, map: (focus: EProcessTemplate) -> R): R {
        return when(source) {
            is EInstance -> foldMap(M, source.template, map)
            is EProcessFinal -> M.empty()
            is EProcessTemplate -> map(source)
            is ETemplateRef -> M.empty()
        }
    }

    override fun modify(
        source: TemplateExpression,
        map: (focus: EProcessTemplate) -> TemplateExpression
    ): TemplateExpression {
        return when(source) {
            is EInstance -> modify(source.template, map)
            is EProcessFinal -> source
            is EProcessTemplate -> map(source)
            is ETemplateRef -> source
        }
    }
}
