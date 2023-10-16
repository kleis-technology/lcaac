package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.expression.ESubstanceSpec
import ch.kleis.lcaac.core.lang.expression.PortExpression

sealed interface Request<Q> {
    val value: PortExpression<Q>
}

data class ProductRequest<Q>(
    val address: Address<Q>,
    override val value: EProductSpec<Q>,
) : Request<Q>

data class SubstanceRequest<Q>(
    val address: Address<Q>,
    override val value: ESubstanceSpec<Q>,
) : Request<Q>
