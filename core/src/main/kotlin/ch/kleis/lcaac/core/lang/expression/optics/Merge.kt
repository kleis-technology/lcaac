package ch.kleis.lcaac.core.lang.expression.optics

import arrow.core.compose
import arrow.optics.Fold
import arrow.optics.PEvery
import arrow.typeclasses.Monoid

class Merge<S, A, B>(
    private val elements: List<PEvery<S, S, A, B>>
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

class MergeFold<S, A>(
    private val elements: List<Fold<S, A>>,
) : Fold<S, A> {
    override fun <R> foldMap(M: Monoid<R>, source: S, map: (focus: A) -> R): R {
        return elements.map { it.foldMap(M, source, map) }
            .reduce { acc, r -> M.fold(listOf(acc, r)) }
    }
}
