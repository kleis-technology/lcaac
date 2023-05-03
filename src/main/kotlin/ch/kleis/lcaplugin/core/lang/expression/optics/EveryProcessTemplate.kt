package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.*

val everyProcessTemplateInTemplateExpression = object : PEvery<ProcessTemplateExpression, ProcessTemplateExpression, EProcessTemplate, ProcessTemplateExpression> {
    override fun <R> foldMap(M: Monoid<R>, source: ProcessTemplateExpression, map: (focus: EProcessTemplate) -> R): R {
        return when(source) {
            is EProcessTemplateApplication -> foldMap(M, source.template, map)
            is EProcessFinal -> M.empty()
            is EProcessTemplate -> map(source)
            is EProcessTemplateRef -> M.empty()
        }
    }

    override fun modify(
        source: ProcessTemplateExpression,
        map: (focus: EProcessTemplate) -> ProcessTemplateExpression
    ): ProcessTemplateExpression {
        return when(source) {
            is EProcessTemplateApplication -> modify(source.template, map)
            is EProcessFinal -> source
            is EProcessTemplate -> map(source)
            is EProcessTemplateRef -> source
        }
    }
}
