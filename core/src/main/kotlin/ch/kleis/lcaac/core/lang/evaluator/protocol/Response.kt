package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.lang.expression.ConnectionExpression
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization

sealed interface Response<Q> {
    val address: Address<Q>
    val value: ConnectionExpression<Q>
}

data class ProductResponse<Q>(
    override val address: PAddr<Q>,
    override val value: EProcess<Q>,
    val selectedPortIndex: Int,
) : Response<Q>

data class SubstanceResponse<Q>(
    override val address: SAddr<Q>,
    override val value: ESubstanceCharacterization<Q>,
) : Response<Q>
