@file:Suppress("NOTHING_TO_INLINE")

package ch.kleis.lcaac.core.lang.expression.optics

import arrow.core.identity
import arrow.core.left
import arrow.core.right
import arrow.optics.Every
import arrow.optics.PEvery
import arrow.typeclasses.Monoid
import ch.kleis.lcaac.core.lang.expression.*

inline fun <E, Q> BlockExpression.Companion.everyDataRef(subOptics: PEvery<E, E, EDataRef<Q>, DataExpression<Q>>) =
    object : PEvery<BlockExpression<E, Q>, BlockExpression<E, Q>, EDataRef<Q>, DataExpression<Q>> {
        override fun <R> foldMap(M: Monoid<R>, source: BlockExpression<E, Q>, map: (focus: EDataRef<Q>) -> R): R {
            return when (source) {
                is EBlockEntry -> M.fold(
                    subOptics.getAll(source.entry).map(map)
                )

                is EBlockForEach -> M.fold(
                    (everyDataExpressionInDataSourceExpression<Q>() compose everyDataRefInDataExpression())
                        .getAll(source.dataSource)
                        .map(map)
                        .plus(
                            source.locals.values.flatMap { everyDataRefInDataExpression<Q>().getAll(it) }.map(map)
                        )
                        .plus(
                            source.body.map { foldMap(M, it, map) }
                        )
                )
            }
        }

        override fun modify(source: BlockExpression<E, Q>, map: (focus: EDataRef<Q>) -> DataExpression<Q>): BlockExpression<E, Q> {
            return when (source) {
                is EBlockEntry -> source.copy(
                    entry = subOptics.modify(source.entry, map)
                )

                is EBlockForEach -> source.copy(
                    dataSource = (everyDataExpressionInDataSourceExpression<Q>() compose everyDataRefInDataExpression())
                        .modify(source.dataSource, map),
                    locals = source.locals.mapValues { everyDataRefInDataExpression<Q>().modify(it.value, map) },
                    body = source.body.map { modify(it, map) },
                )
            }
        }
    }


inline fun <E, Q> BlockExpression.Companion.everyEntry(): Every<BlockExpression<E, Q>, E> =
    object : Every<BlockExpression<E, Q>, E> {
        override fun <R> foldMap(M: Monoid<R>, source: BlockExpression<E, Q>, map: (focus: E) -> R): R {
            return when (source) {
                is EBlockEntry -> map(source.entry)
                is EBlockForEach -> M.fold(
                    source.body.map { foldMap(M, it, map) }
                )
            }
        }

        override fun modify(source: BlockExpression<E, Q>, map: (focus: E) -> E): BlockExpression<E, Q> {
            return when (source) {
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
