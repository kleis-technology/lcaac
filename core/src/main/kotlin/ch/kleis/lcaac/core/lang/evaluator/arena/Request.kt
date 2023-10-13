package ch.kleis.lcaac.core.lang.evaluator.arena

import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.expression.ESubstanceSpec
import ch.kleis.lcaac.core.lang.expression.PortExpression

sealed interface Request<Q> {
    val value: PortExpression<Q>
}

data class ProductRequest<Q>(
    val address: PAddr<Q>,
    override val value: EProductSpec<Q>,
) : Request<Q>

data class SubstanceRequest<Q>(
    val address: SAddr<Q>,
    override val value: ESubstanceSpec<Q>,
) : Request<Q>
