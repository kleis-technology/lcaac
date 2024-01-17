package ch.kleis.lcaac.core.testing

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.math.basic.BasicNumber

data class TestCase<S>(
    val source: S,
    val name: String,
    val assertions: List<RangeAssertion>,
    val template: EProcessTemplate<BasicNumber>,
    val arguments: Map<String, DataExpression<BasicNumber>> = emptyMap(),
)
