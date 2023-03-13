package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.core.compose
import arrow.optics.PEvery
import arrow.typeclasses.Monoid

data class Merge<S, A, B>(
    val elements: List<PEvery<S, S, A, B>>
) : PEvery<S, S, A, B> {
    override fun <R> foldMap(M: Monoid<R>, source: S, map: (focus: A) -> R): R {
        return elements.map { it.foldMap(M, source, map) }
            .reduce { acc, r -> M.fold(listOf(acc, r)) }
    }

    override fun modify(source: S, map: (focus: A) -> B): S {
        val update = elements
            .map { it.lift(map) }
            .reduce { acc, function -> acc.compose(function) }
        return update(source)
    }
}
