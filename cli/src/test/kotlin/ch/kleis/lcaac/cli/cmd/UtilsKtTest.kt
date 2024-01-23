package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EQuantityMul
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.prelude.Prelude
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals


class UtilsKtTest {

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
