package ch.kleis.lcaac.core.testing

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.math.basic.BasicNumber

data class RangeAssertion(
    val ref: String,
    val lo: DataExpression<BasicNumber>,
    val hi: DataExpression<BasicNumber>,
)
