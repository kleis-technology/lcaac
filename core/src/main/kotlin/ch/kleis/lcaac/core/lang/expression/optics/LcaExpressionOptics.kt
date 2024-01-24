@file:Suppress("NOTHING_TO_INLINE")

package ch.kleis.lcaac.core.lang.expression.optics

import arrow.core.identity
import arrow.core.left
import arrow.core.right
import arrow.optics.Every
import arrow.typeclasses.Monoid
import ch.kleis.lcaac.core.lang.expression.*

inline fun <E, Q> BlockExpression.Companion.everyEntry(): Every<BlockExpression<E, Q>, E> =
    object : Every<BlockExpression<E, Q>, E> {
        override fun <R> foldMap(M: Monoid<R>, source: BlockExpression<E, Q>, map: (focus: E) -> R): R {
            return when(source) {
                is EBlockEntry -> map(source.entry)
                is EBlockForEach -> M.fold(
                    source.body.map { foldMap(M, it, map) }
                )
            }
        }

        override fun modify(source: BlockExpression<E, Q>, map: (focus: E) -> E): BlockExpression<E, Q> {
            return when(source) {
                is EBlockEntry -> EBlockEntry(map(source.entry))
                is EBlockForEach -> EBlockForEach(
                    source.rowRef,
                    source.dataSource,
                    source.locals,
                    source.body.map {
                        modify(it, map)
                    }
                )
            }
        }
    }

inline fun <Q> LcaExpression.Companion.eProcess(): arrow.optics.Prism<LcaExpression<Q>, EProcess<Q>> =
    arrow.optics.Prism(
        getOrModify = { lcaExpression: LcaExpression<Q> ->
            when (lcaExpression) {
                is EProcess<Q> -> lcaExpression.right()
                else -> lcaExpression.left()
            }
        },
        reverseGet = ::identity
    )

inline fun <Q> LcaExpression.Companion.eSubstanceCharacterization(): arrow.optics.Prism<LcaExpression<Q>, ESubstanceCharacterization<Q>> =
    arrow.optics.Prism(
        getOrModify = { lcaExpression: LcaExpression<Q> ->
            when (lcaExpression) {
                is ESubstanceCharacterization<Q> -> lcaExpression.right()
                else -> lcaExpression.left()
            }
        },
        reverseGet = ::identity,
    )

inline fun <Q> LcaExpression.Companion.eProductSpec(): arrow.optics.Prism<LcaExpression<Q>, EProductSpec<Q>> =
    arrow.optics.Prism(
        getOrModify = { lcaExpression: LcaExpression<Q> ->
            when (lcaExpression) {
                is EProductSpec<Q> -> lcaExpression.right()
                else -> lcaExpression.left()
            }
        },
        reverseGet = ::identity
    )


inline fun <Q> LcaExpression.Companion.lcaExchangeExpression(): arrow.optics.Prism<LcaExpression<Q>, LcaExchangeExpression<Q>> =
    arrow.optics.Prism(
        getOrModify = { lcaExpression: LcaExpression<Q> ->
            when (lcaExpression) {
                is LcaExchangeExpression -> lcaExpression.right()
                else -> lcaExpression.left()
            }
        },
        reverseGet = ::identity
    )
