package ch.kleis.lcaac.core.testing

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.fixture.ProcessFixture
import ch.kleis.lcaac.core.lang.fixture.UnitFixture
import ch.kleis.lcaac.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class BasicTestRunnerTest {

    @Test
    fun run_rangeAssertion_whenSuccess() {
        // given
        val carrotProduction = EProcessTemplate(
            body = ProcessFixture.carrotProduction
        )
        val case = TestCase(
            source = "source",
            name = "carrot_production",
            assertions = listOf(
                RangeAssertion(
                    "Climate Change",
                    EQuantityScale(BasicNumber(0.5), UnitFixture.kg),
                    EQuantityScale(BasicNumber(2.0), UnitFixture.kg),
                )
            ),
            template = carrotProduction,
            arguments = emptyMap(),
        )
        val symbolTable = SymbolTable.empty<BasicNumber>()
        val runner = BasicTestRunner<String>(symbolTable)

        // when
        val actual = runner.run(case)

        // then
        assertEquals("source", actual.source)
        assertEquals("carrot_production", actual.name)
        assertEquals(1, actual.results.size)
        assertEquals(
            RangeAssertionSuccess(
                name = "Climate Change",
                lo = QuantityValue(BasicNumber(0.5), UnitValueFixture.kg()),
                hi = QuantityValue(BasicNumber(2.0), UnitValueFixture.kg()),
                actual = QuantityValue(BasicNumber(1.0), UnitValueFixture.kg()),
            ),
            actual.results[0]
        )
    }

    @Test
    fun run_rangeAssertion_whenRangeFailure() {
        // given
        val carrotProduction = EProcessTemplate(
            body = ProcessFixture.carrotProduction
        )
        val case = TestCase(
            source = "source",
            name = "carrot_production",
            assertions = listOf(
                RangeAssertion(
                    "Climate Change",
                    EQuantityScale(BasicNumber(50.0), UnitFixture.kg),
                    EQuantityScale(BasicNumber(200.0), UnitFixture.kg),
                )
            ),
            template = carrotProduction,
            arguments = emptyMap(),
        )
        val symbolTable = SymbolTable.empty<BasicNumber>()
        val runner = BasicTestRunner<String>(symbolTable)

        // when
        val actual = runner.run(case)

        // then
        assertEquals("source", actual.source)
        assertEquals("carrot_production", actual.name)
        assertEquals(1, actual.results.size)
        assertEquals(
            RangeAssertionFailure(
                name = "Climate Change",
                lo = QuantityValue(BasicNumber(50.0), UnitValueFixture.kg()),
                hi = QuantityValue(BasicNumber(200.0), UnitValueFixture.kg()),
                actual = QuantityValue(BasicNumber(1.0), UnitValueFixture.kg()),
            ),
            actual.results[0]
        )
    }

    @Test
    fun run_rangeAssertion_whenUnknownRef() {
        // given
        val carrotProduction = EProcessTemplate(
            body = ProcessFixture.carrotProduction
        )
        val case = TestCase(
            source = "source",
            name = "carrot_production",
            assertions = listOf(
                RangeAssertion(
                    "foo",
                    EQuantityScale(BasicNumber(50.0), UnitFixture.kg),
                    EQuantityScale(BasicNumber(200.0), UnitFixture.kg),
                )
            ),
            template = carrotProduction,
            arguments = emptyMap(),
        )
        val symbolTable = SymbolTable.empty<BasicNumber>()
        val runner = BasicTestRunner<String>(symbolTable)

        // when
        val actual = runner.run(case)

        // then
        assertEquals("source", actual.source)
        assertEquals("carrot_production", actual.name)
        assertEquals(1, actual.results.size)
        assertEquals(
            GenericFailure("unknown reference 'foo'"),
            actual.results[0]
        )
    }

    @Test
    fun run_whenEvaluatorException() {
        // given
        val carrotProduction = EProcessTemplate(
            body = ProcessFixture.carrotProduction
        )
        val evaluator = mockk<Evaluator<BasicNumber>>()
        every { evaluator.with(carrotProduction) } returns evaluator
        every { evaluator.trace(carrotProduction) } throws EvaluatorException("some error")
        val runner = BasicTestRunner<String>(SymbolTable.empty(), evaluator)
        val case = TestCase(
            source = "source",
            name = "carrot_production",
            assertions = listOf(
                RangeAssertion(
                    "Climate Change",
                    EQuantityScale(BasicNumber(50.0), UnitFixture.kg),
                    EQuantityScale(BasicNumber(200.0), UnitFixture.kg),
                )
            ),
            template = carrotProduction,
            arguments = emptyMap(),
        )

        // when
        val actual = runner.run(case)

        // then
        assertEquals("source", actual.source)
        assertEquals("carrot_production", actual.name)
        assertEquals(1, actual.results.size)
        assertEquals(
            GenericFailure("some error"),
            actual.results[0]
        )
    }
}
