package ch.kleis.lcaac.core.testing

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.source.SourceMap
import ch.kleis.lcaac.core.math.basic.BasicNumber

data class TestCase<S>(
    val source: S,
    val name: String,
    val assertions: List<RangeAssertion>,
    val template: EProcessTemplate<BasicNumber>,
    val arguments: Map<String, DataExpression<BasicNumber>> = emptyMap(),
    /** Locations of the test definition, its variables and given exchanges. */
    val sourceMap: SourceMap = SourceMap.empty(),
) {
    companion object {
        private const val PROCESS_PREFIX = "__test__"

        /** The name of the process generated for a test. */
        fun processName(test: String): String = "$PROCESS_PREFIX$test"

        /** The name of the product of the process generated for a test. */
        fun productName(test: String): String = "${PROCESS_PREFIX}product__$test"
    }
}
