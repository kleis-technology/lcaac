package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.optics.Every
import arrow.optics.PPrism
import ch.kleis.lcaplugin.core.lang.expression.*

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

val everyProductRef: Every<Expression, EProductRef> =
    Merge(
        listOf(
            Expression.lcaExpression.lcaProductExpression compose productRefInProductExpression,
            Expression.lcaExpression.lcaExchangeExpression.eTechnoExchange.product compose productRefInProductExpression,
            Expression.lcaExpression.lcaProcessExpression.eProcess.products compose Every.list() compose
                ETechnoExchange.product compose productRefInProductExpression,
            Expression.lcaExpression.lcaProcessExpression.eProcess.inputs compose Every.list() compose
                    ETechnoExchange.product compose productRefInProductExpression,
        )
    )
