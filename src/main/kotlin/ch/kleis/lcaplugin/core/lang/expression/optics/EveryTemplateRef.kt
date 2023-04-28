package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.Every
import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.*

val everyTemplateRefInTemplateExpression =
    object : PEvery<ProcessTemplateExpression, ProcessTemplateExpression, EProcessTemplateRef, ProcessTemplateExpression> {
        override fun <R> foldMap(M: Monoid<R>, source: ProcessTemplateExpression, map: (focus: EProcessTemplateRef) -> R): R {
            return when (source) {
                is EProcessTemplateApplication -> foldMap(M, source.template, map)
                is EProcessFinal -> M.empty()
                is EProcessTemplate -> M.empty()
                is EProcessTemplateRef -> map(source)
            }
        }

        override fun modify(
            source: ProcessTemplateExpression,
            map: (focus: EProcessTemplateRef) -> ProcessTemplateExpression
        ): ProcessTemplateExpression {
            return when (source) {
                is EProcessTemplateApplication -> modify(source.template, map)
                is EProcessFinal -> source
                is EProcessTemplate -> source
                is EProcessTemplateRef -> map(source)
            }
        }

    }

val everyTemplateRefInProcess: Every<EProcess, EProcessTemplateRef> =
    Merge(
        listOf(
            EProcess.products compose Every.list() compose ETechnoExchange.product.constraint,
            EProcess.inputs compose Every.list() compose ETechnoExchange.product.constraint,
        )
    ) compose Constraint.fromProcessRef.ref compose EProcessTemplateRef.iso.reverse()


val everyTemplateRef: Every<Expression, EProcessTemplateRef> =
    Merge(
        listOf(
            Expression.processTemplateExpression compose everyTemplateRefInTemplateExpression,
            Expression.lcaExpression.lcaExchangeExpression.eTechnoExchange.product.constraint.fromProcessRef.ref compose
                    EProcessTemplateRef.iso.reverse(),
            Expression.lcaExpression.eProcess compose everyTemplateRefInProcess,
            Expression.systemExpression.eSystem.processes compose Every.list() compose everyTemplateRefInProcess,
        )
    )
