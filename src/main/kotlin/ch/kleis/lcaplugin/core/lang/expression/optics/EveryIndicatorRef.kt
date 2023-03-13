package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.optics.Every
import arrow.optics.PPrism
import ch.kleis.lcaplugin.core.lang.expression.*

val indicatorRefInIndicatorExpression =
    object : PPrism<LcaIndicatorExpression, LcaIndicatorExpression, EIndicatorRef, LcaIndicatorExpression> {
        override fun getOrModify(source: LcaIndicatorExpression): Either<LcaIndicatorExpression, EIndicatorRef> {
            return when (source) {
                is EIndicator -> source.left()
                is EIndicatorRef -> source.right()
            }
        }

        override fun reverseGet(focus: LcaIndicatorExpression): LcaIndicatorExpression {
            return focus
        }
    }

val everyIndicatorRef: Every<Expression, EIndicatorRef> =
    Merge(
        listOf(
            Expression.lcaExpression.lcaIndicatorExpression compose indicatorRefInIndicatorExpression,
            Expression.lcaExpression.lcaExchangeExpression.eImpact.indicator compose indicatorRefInIndicatorExpression,
            Expression.lcaExpression.lcaSubstanceCharacterizationExpression.eSubstanceCharacterization.impacts compose
                    Every.list() compose EImpact.indicator compose indicatorRefInIndicatorExpression,
            Expression.systemExpression.eSystem.substanceCharacterizations compose
                    Every.list() compose LcaSubstanceCharacterizationExpression.eSubstanceCharacterization.impacts compose
                    Every.list() compose EImpact.indicator compose indicatorRefInIndicatorExpression,
        )
    )

