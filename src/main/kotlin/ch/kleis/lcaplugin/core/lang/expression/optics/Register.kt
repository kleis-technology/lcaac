package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.Every
import arrow.optics.typeclasses.FilterIndex
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.Register

fun <E> everyRegister() =
    object : Every<Register<E>, E> {
        override fun <R> foldMap(M: Monoid<R>, source: Register<E>, map: (focus: E) -> R): R {
            return M.fold(
                source.values.map(map)
            )
        }

        override fun modify(source: Register<E>, map: (focus: E) -> E): Register<E> {
            return Register(source.mapValues { map(it.value) })
        }

    }
