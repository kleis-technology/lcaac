package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.optics.Every
import arrow.optics.PEvery
import arrow.optics.PPrism
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.processTemplates
import ch.kleis.lcaplugin.core.lang.products

private val productRefInUnconstrainedProductExpression =
    object :
        PPrism<LcaUnconstrainedProductExpression, LcaUnconstrainedProductExpression, EProductRef, LcaUnconstrainedProductExpression> {
        override fun getOrModify(source: LcaUnconstrainedProductExpression): Either<LcaUnconstrainedProductExpression, EProductRef> {
            return when (source) {
                is EProduct -> source.left()
                is EProductRef -> source.right()
            }
        }

        override fun reverseGet(focus: LcaUnconstrainedProductExpression): LcaUnconstrainedProductExpression {
            return focus
        }
    }

val productRefInProductExpression =
    Merge(
        listOf(
            LcaProductExpression.lcaUnconstrainedProductExpression compose productRefInUnconstrainedProductExpression,
            LcaProductExpression.eConstrainedProduct.product compose productRefInUnconstrainedProductExpression,
        )
    )

val productRefInExchangeExpression =
    LcaExchangeExpression.eTechnoExchange.product compose productRefInProductExpression

val everyProductRefInProcess: PEvery<EProcess, EProcess, EProductRef, LcaUnconstrainedProductExpression> =
    Merge(
        listOf(
            EProcess.products compose Every.list() compose ETechnoExchange.product compose productRefInProductExpression,
            EProcess.inputs compose Every.list() compose ETechnoExchange.product compose productRefInProductExpression,
        )
    )

val everyProductRefInProcessExpression =
    LcaProcessExpression.eProcess compose everyProductRefInProcess

private val everyProductRefInSystemExpression =
    SystemExpression.eSystem.processes compose
            Every.list() compose
            everyProductRefInProcessExpression

val everyProductRef: Every<Expression, EProductRef> =
    Merge(
        listOf(
            Expression.lcaExpression.lcaProductExpression compose productRefInProductExpression,
            Expression.lcaExpression.lcaExchangeExpression compose productRefInExchangeExpression,
            Expression.lcaExpression.lcaProcessExpression compose everyProductRefInProcessExpression,
            Expression.systemExpression compose everyProductRefInSystemExpression,
            Expression.templateExpression compose everyProcessTemplateInTemplateExpression compose
                    EProcessTemplate.body compose everyProductRefInProcessExpression,
            Expression.templateExpression.eProcessFinal.expression compose everyProductRefInProcessExpression,
        )
    )

val everyProductRefInSymbolTable: PEvery<SymbolTable, SymbolTable, EProductRef, LcaUnconstrainedProductExpression> =
    Merge(
        listOf(
            SymbolTable.products compose everyRegister() compose productRefInUnconstrainedProductExpression,
            SymbolTable.processTemplates compose everyRegister() compose
                    everyProcessTemplateInTemplateExpression compose
                    EProcessTemplate.body compose everyProductRefInProcessExpression,
        )
    )
