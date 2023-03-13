package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics

@optics
sealed interface SystemExpression : Expression {
    companion object
}

@optics
data class ESystem(
    val processes: List<LcaProcessExpression>,
    val substanceCharacterizations: List<LcaSubstanceCharacterizationExpression>,
) : SystemExpression {
    companion object
}
