package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.optics.Every
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec

object EverySubstanceNameAndCompartment : Every<ESubstanceSpec, Pair<String, String?>> {
    override fun <R> foldMap(M: Monoid<R>, source: ESubstanceSpec, map: (focus: Pair<String, String?>) -> R): R {
        return map(Pair(source.name, source.compartment))
    }

    override fun modify(
        source: ESubstanceSpec,
        map: (focus: Pair<String, String?>) -> Pair<String, String?>
    ): ESubstanceSpec {
        val (name, compartment) = map(Pair(source.name, source.compartment))
        return source.copy(
            name = name,
            compartment = compartment,
        )
    }
}