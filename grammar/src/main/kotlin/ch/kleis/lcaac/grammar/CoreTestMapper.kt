package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.testing.RangeAssertion
import ch.kleis.lcaac.core.testing.TestCase
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import org.antlr.v4.runtime.tree.TerminalNode

class CoreTestMapper {
    private val coreMapper = CoreMapper(BasicOperations)

    fun test(
        ctx: LcaLangParser.TestDefinitionContext,
    ): TestCase<LcaLangParser.TestDefinitionContext> {
        val name = ctx.testRef().uid().ID().innerText()
        val processName = "__test__${name}"
        val productName = "__test__product__${name}"
        return TestCase(
            source = ctx,
            name = name,
            assertions = ctx.block_assert().flatMap {
                it.rangeAssertion().map { assertion ->
                    val ref = assertion.uid().ID().innerText()
                    val lo = coreMapper.dataExpression(assertion.dataExpression(0))
                    val hi = coreMapper.dataExpression(assertion.dataExpression(1))
                    RangeAssertion(ref, lo, hi)
                }
            },
            template = EProcessTemplate(
                body = EProcess(
                    name = processName,
                    products = listOf(
                        ETechnoExchange(
                            EQuantityScale(BasicNumber(1.0), EDataRef("u")),
                            EProductSpec(productName, EDataRef("u"))
                        )
                    ),
                    inputs = ctx.block_given().flatMap {
                        it.technoInputExchange().map { coreMapper.technoInputExchange(it) }
                    }
                )
            ),
            arguments = emptyMap()
        )
    }

    private fun TerminalNode.innerText(): String {
        return this.text.trim('"')
    }
}
