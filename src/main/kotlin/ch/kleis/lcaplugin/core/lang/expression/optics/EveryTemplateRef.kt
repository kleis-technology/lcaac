package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.Every
import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.Environment
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.processTemplates

val everyTemplateRefInTemplateExpression =
    object : PEvery<TemplateExpression, TemplateExpression, ETemplateRef, TemplateExpression> {
        override fun <R> foldMap(M: Monoid<R>, source: TemplateExpression, map: (focus: ETemplateRef) -> R): R {
            return when (source) {
                is EInstance -> foldMap(M, source.template, map)
                is EProcessFinal -> M.empty()
                is EProcessTemplate -> M.empty()
                is ETemplateRef -> map(source)
            }
        }

        override fun modify(
            source: TemplateExpression,
            map: (focus: ETemplateRef) -> TemplateExpression
        ): TemplateExpression {
            return when (source) {
                is EInstance -> modify(source.template, map)
                is EProcessFinal -> source
                is EProcessTemplate -> source
                is ETemplateRef -> map(source)
            }
        }

    }

val everyTemplateRefInProcess: Every<EProcess, ETemplateRef> =
    Merge(
        listOf(
            EProcess.products compose Every.list() compose ETechnoExchange.product.eConstrainedProduct.constraint,
            EProcess.inputs compose Every.list() compose ETechnoExchange.product.eConstrainedProduct.constraint,
        )
    ) compose Constraint.fromProcessRef.template

val everyTemplateRefInProcessExpression =
    LcaProcessExpression.eProcess compose everyTemplateRefInProcess

val everyTemplateRefInEnvironment: Every<Environment, ETemplateRef> =
    Environment.processTemplates compose everyRegister() compose
            everyProcessTemplateInTemplateExpression compose
            EProcessTemplate.body compose
            everyTemplateRefInProcessExpression


val everyTemplateRef: Every<Expression, ETemplateRef> =
    Merge(
        listOf(
            Expression.templateExpression compose everyTemplateRefInTemplateExpression,
            Expression.lcaExpression.lcaExchangeExpression.eTechnoExchange.product.eConstrainedProduct.constraint.fromProcessRef.template,
            Expression.lcaExpression.lcaProcessExpression compose everyTemplateRefInProcessExpression,
            Expression.systemExpression.eSystem.processes compose Every.list() compose everyTemplateRefInProcessExpression,
        )
    )
