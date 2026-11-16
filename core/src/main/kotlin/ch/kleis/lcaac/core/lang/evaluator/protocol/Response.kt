package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.lang.evaluator.derivation.ProcessDerivation
import ch.kleis.lcaac.core.lang.evaluator.derivation.ProcessOrigins
import ch.kleis.lcaac.core.lang.expression.ConnectionExpression
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization

/*
    ConnectionExpression is a common interface for a EProcess or a ESubstanceCharacterization
    PortExpression is a common interface for a EProductSpec, ESubstanceSpec or EIndicatorSpec
 */

sealed interface Response<Q> {
    val address: Address<Q>
    val value: ConnectionExpression<Q>
}

data class ProductResponse<Q>(
    override val address: Address<Q>,
    override val value: EProcess<Q>,
    val productInProcessIndex: Int,
    /** Where the exchanges of [value] come from, unless it is a cached process. */
    val origins: ProcessOrigins? = null,
    /** How [value] was evaluated, when the evaluator records derivations. */
    val derivation: ProcessDerivation<Q>? = null,
) : Response<Q>

data class SubstanceResponse<Q>(
    override val address: Address<Q>,
    override val value: ESubstanceCharacterization<Q>,
) : Response<Q>
