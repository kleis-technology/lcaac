package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.fixture.DimensionFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.pow

class UnitExpressionReducerTest {

    @Test
    fun reduce_whenLiteral_shouldReturnSame() {
        // given
        val kg = UnitFixture.kg
        val reducer = UnitExpressionReducer(Environment.empty())

        // when
        val actual = reducer.reduce(kg)

        // then
        assertEquals(kg, actual)
    }

    @Test
    fun reduce_whenDiv_shouldDivide() {
        // given
        val kg = UnitFixture.kg
        val l = UnitFixture.l
        val reducer = UnitExpressionReducer(Environment.empty())

        // when
        val actual = reducer.reduce(EUnitDiv(kg, l))

        // then
        val expected = EUnitLiteral(
            "kg/l",
            1.0 / 1.0e-3,
            DimensionFixture.mass.divide(DimensionFixture.volume),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenMul_shouldMultiply() {
        // given
        val kg = UnitFixture.kg
        val l = UnitFixture.l
        val reducer = UnitExpressionReducer(Environment.empty())

        // when
        val actual = reducer.reduce(EUnitMul(kg, l))

        // then
        val expected = EUnitLiteral(
            "kg.l",
            1.0 * 1.0e-3,
            DimensionFixture.mass.multiply(DimensionFixture.volume),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPow_shouldPow() {
        // given
        val m = UnitFixture.m
        val reducer = UnitExpressionReducer(Environment.empty())

        // when
        val actual = reducer.reduce(EUnitPow(m, 2.0))

        // then
        val expected = EUnitLiteral(
            "m^(2.0)",
            1.0.pow(2.0),
            DimensionFixture.length.pow(2.0),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenRef_shouldReadEnv() {
        // given
        val ref = EUnitRef("kg")
        val environment = Environment<UnitExpression>(
            hashMapOf(
                Pair("kg", UnitFixture.kg)
            )
        )
        val reducer = UnitExpressionReducer(environment)

        // when
        val actual = reducer.reduce(ref)

        // then
        assertEquals(UnitFixture.kg, actual)
    }
}
