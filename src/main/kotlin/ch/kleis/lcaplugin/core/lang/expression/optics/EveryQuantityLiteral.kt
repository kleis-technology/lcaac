package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.Every
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.*

val everyQuantityLiteralInQuantityExpression =
    object : Every<QuantityExpression, EQuantityLiteral> {
        override fun <R> foldMap(M: Monoid<R>, source: QuantityExpression, map: (focus: EQuantityLiteral) -> R): R {
            return when (source) {
                is EQuantityAdd -> M.fold(
                    listOf(
                        foldMap(M, source.left, map),
                        foldMap(M, source.right, map),
                    )
                )

                is EQuantityDiv -> M.fold(
                    listOf(
                        foldMap(M, source.left, map),
                        foldMap(M, source.right, map),
                    )
                )

                is EQuantityLiteral -> map(source)
                is EQuantityMul -> M.fold(
                    listOf(
                        foldMap(M, source.left, map),
                        foldMap(M, source.right, map),
                    )
                )

                is EQuantityNeg -> foldMap(M, source.quantity, map)
                is EQuantityPow -> foldMap(M, source.quantity, map)
                is EQuantityRef -> M.empty()
                is EQuantitySub -> M.fold(
                    listOf(
                        foldMap(M, source.left, map),
                        foldMap(M, source.right, map),
                    )
                )

                is EQuantityScale -> foldMap(M, source.quantity, map)
            }
        }

        override fun modify(
            source: QuantityExpression,
            map: (focus: EQuantityLiteral) -> EQuantityLiteral
        ): QuantityExpression {
            return when (source) {
                is EQuantityAdd -> EQuantityAdd(
                    modify(source.left, map),
                    modify(source.right, map),
                )

                is EQuantityDiv -> EQuantityDiv(
                    modify(source.left, map),
                    modify(source.right, map),
                )

                is EQuantityLiteral -> map(source)
                is EQuantityMul -> EQuantityMul(
                    modify(source.left, map),
                    modify(source.right, map),
                )

                is EQuantityNeg -> EQuantityNeg(
                    modify(source.quantity, map),
                )

                is EQuantityPow -> EQuantityPow(
                    modify(source.quantity, map),
                    source.exponent,
                )

                is EQuantityRef -> source
                is EQuantitySub -> EQuantitySub(
                    modify(source.left, map),
                    modify(source.right, map),
                )

                is EQuantityScale -> EQuantityScale(
                    source.scale,
                    modify(source.quantity, map),
                )
            }
        }
    }
