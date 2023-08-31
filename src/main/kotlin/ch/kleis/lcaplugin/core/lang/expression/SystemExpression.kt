package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics

@optics
sealed interface SystemExpression<Q> : Expression<Q> {
    companion object
}

@optics
data class ESystem<Q>(
    val processes: List<EProcess<Q>>,
    val substanceCharacterizations: List<ESubstanceCharacterization<Q>>,
) : SystemExpression<Q> {
    companion object
}
