package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.Every
import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.*

private val everyStringRefInStringExpression =
    object : PEvery<StringExpression, StringExpression, EStringRef, StringExpression> {
        override fun <R> foldMap(M: Monoid<R>, source: StringExpression, map: (focus: EStringRef) -> R): R {
            return when (source) {
                is EStringLiteral -> M.empty()
                is EStringRef -> map(source)
            }
        }

        override fun modify(source: StringExpression, map: (focus: EStringRef) -> StringExpression): StringExpression {
            return when (source) {
                is EStringLiteral -> source
                is EStringRef -> map(source)
            }
        }
    }

private val everyStringRefInEProductSpec: PEvery<EProductSpec, EProductSpec, EStringRef, StringExpression> =
    Merge(
        listOf(
            EProductSpec.fromProcess.matchLabels.elements compose
                Every.map() compose
                everyStringRefInStringExpression,
            EProductSpec.fromProcess.arguments compose
                Every.map() compose
                DataExpression.stringExpression compose
                everyStringRefInStringExpression,
        )
    )

val everyStringRefInEProcess: PEvery<EProcess, EProcess, EStringRef, StringExpression> =
    Merge(
        listOf(
            EProcess.products,
            EProcess.inputs,
        )
    ) compose
        Every.list() compose
        ETechnoExchange.product compose everyStringRefInEProductSpec

