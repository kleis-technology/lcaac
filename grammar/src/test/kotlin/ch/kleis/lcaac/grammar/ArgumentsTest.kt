package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EQuantityMul
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.assertThrows

class ArgumentsTest {
    @Test
    fun dataExpressionMap_whenTrailingInput_shouldThrow() {
        // when/then
        val actual = assertThrows<SyntaxException> {
            dataExpressionMap(BasicOperations, mapOf("efficiency" to "80 percent)"))
        }
        assertEquals("<global efficiency>:1:11: unexpected input ')'", actual.message)
    }

    @Test
    fun parseOverrides_whenGivenValueFails_shouldReportTheArgument() {
        // given
        val reducer = DataExpressionReducer<BasicNumber>(Prelude.units(), DataSourceRegister.empty(), BasicOperations, mockk())
        val current = mapOf<String, DataExpression<BasicNumber>>("factor" to EQuantityScale(BasicNumber(2.0), Prelude.unitMap<BasicNumber>()["kg"]!!))

        // when/then
        val actual = assertThrows<EvaluatorException> { parseOverrides(reducer, current, mapOf("factor" to "1 kg + 1 kWh")) }
        assertEquals(
            "incompatible dimensions: mass vs energy in left=kg and right=kWh\n  in argument 'factor' = 1 kg + 1 kWh",
            actual.describe(),
        )
    }

    @Test
    fun parseQuantityWithDefaultUnit_invalidExpression() {
        // given
        val s = "a@bc"
        val defaultUnit = Prelude.unitMap<BasicNumber>()["kg"]!!

        // when/then
        val actual = assertThrows<EvaluatorException> { smartParseQuantityWithDefaultUnit(s, defaultUnit) }
        assertEquals("'a@bc' is not a valid quantity", actual.message)
    }

    @Test
    fun parseQuantityWithDefaultUnit_invalidExpression_multipleParts() {
        // given
        val s = "12 3 4"
        val defaultUnit = Prelude.unitMap<BasicNumber>()["kg"]!!

        // when/then
        val actual = assertThrows<EvaluatorException> { smartParseQuantityWithDefaultUnit(s, defaultUnit) }
        assertEquals("'12 3 4' is not a valid quantity", actual.message)
    }

    @Test
    fun parseQuantityWithDefaultUnit_invalidExpression_invalidUnit() {
        // given
        val s = "12 $3"
        val defaultUnit = Prelude.unitMap<BasicNumber>()["kg"]!!

        // when/then
        val actual = assertThrows<EvaluatorException> { smartParseQuantityWithDefaultUnit(s, defaultUnit) }
        assertEquals("'12 \$3' is not a valid quantity", actual.message)
    }

    @Test
    fun parseQuantityWithDefaultUnit_whenNumber() {
        // given
        val s = "12.0"
        val defaultUnit = Prelude.unitMap<BasicNumber>()["kg"]!!

        // when
        val actual = smartParseQuantityWithDefaultUnit(s, defaultUnit)

        // then
        assertEquals(EQuantityScale(BasicNumber(12.0), defaultUnit), actual)
    }

    @Test
    fun parseQuantityWithDefaultUnit_whenExpression() {
        // given
        val s = "12.0 kg"
        val defaultUnit = Prelude.unitMap<BasicNumber>()["kg"]!!

        // when
        val actual = smartParseQuantityWithDefaultUnit(s, defaultUnit)

        // then
        assertEquals(EQuantityScale(BasicNumber(12.0), EDataRef("kg")), actual)
    }

    @Test
    fun parseQuantityWithDefaultUnit_whenComplexExpression() {
        // given
        val s = "12.0 kg * hour"
        val kg = Prelude.unitMap<BasicNumber>()["kg"]!!
        val hour = Prelude.unitMap<BasicNumber>()["hour"]!!
        val defaultUnit = EQuantityMul(kg, hour)

        // when
        val actual = smartParseQuantityWithDefaultUnit(s, defaultUnit)

        // then
        assertEquals(
            EQuantityScale(
                BasicNumber(12.0),
                EQuantityMul(EDataRef("kg"), EDataRef("hour"))),
            actual)
    }
}
