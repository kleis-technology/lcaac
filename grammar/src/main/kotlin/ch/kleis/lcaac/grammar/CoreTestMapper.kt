package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.source.SourcePath
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
        val processName = TestCase.processName(name)
        val productName = TestCase.productName(name)
        val owner = SourcePath.Process(ProcessKey(processName))
        // Shared by the expressions and their locations, so that they are numbered the same way
        val given = ctx.block_given().flatMap { it.technoInputExchange() }
        val assertions = ctx.block_assert().flatMap { it.rangeAssertion() }
        return TestCase(
            source = ctx,
            name = name,
            assertions = assertions.mapIndexed { i, assertion ->
                val ref = assertion.uid().ID().innerText()
                val lo = coreMapper.dataExpression(assertion.dataExpression(0))
                val hi = coreMapper.dataExpression(assertion.dataExpression(1))
                RangeAssertion(ref, lo, hi, SourcePath.Assertion(owner, i))
            },
            template = EProcessTemplate(
                locals = ctx.variables().flatMap { it.assignment() }
                    .associate { it.dataRef().uid().ID().text to coreMapper.dataExpression(it.dataExpression()) },
                body = EProcess(
                    name = processName,
                    products = listOf(
                        ETechnoExchange(
                            EQuantityScale(BasicNumber(1.0), EDataRef("u")),
                            EProductSpec(productName, EDataRef("u"))
                        )
                    ),
                    inputs = given.map { coreMapper.technoInputExchange(it) }
                )
            ),
            arguments = emptyMap(),
            sourceMap = coreMapper.testLocations(ctx, name, owner, given, assertions),
        )
    }

    private fun TerminalNode.innerText(): String {
        return this.text.trim('"')
    }
}
