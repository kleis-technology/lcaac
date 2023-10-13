package ch.kleis.lcaac.core.prelude

import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.QuantityValueOperations
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PreludeTest {
    private val ops = BasicOperations

    @Test
    fun test_sanitize() {
        // Given
        val data = listOf(
            "01" to "_01",
            "ab" to "ab",
            "a_+__b" to "a_p_b",
            "a_*__b" to "a_m_b",
            "m*2a" to "m_m_2a",
            "a&b" to "a_a_b",
            "  a&b++" to "a_a_b_p_p",
        )

        data.forEach { (given, expected) ->
            assertEquals(expected, Prelude.sanitize(given))
        }
    }

    @Test
    fun energyAndPower() {
        // given
        val wattHour = with(ToValue(ops)) { Prelude.unitMap<BasicNumber>()["Wh"]!!.toUnitValue() }
        val joule = with(ToValue(ops)) { Prelude.unitMap<BasicNumber>()["J"]!!.toUnitValue() }
        val a = QuantityValue(BasicOperations.pure(1.0), wattHour)
        val b = QuantityValue(BasicOperations.pure(3600.0), joule)

        // when/then
        with(QuantityValueOperations(ops)) {
            assertEquals(a.absoluteScaleValue(), b.absoluteScaleValue())
        }
    }

    @Test
    fun energyAndPowerAndTime() {
        // given
        val watt = with(ToValue(ops)) { Prelude.unitMap<BasicNumber>()["W"]!!.toUnitValue() }
        val hour = with(ToValue(ops)) { Prelude.unitMap<BasicNumber>()["hour"]!!.toUnitValue() }
        val wattHour = with(ToValue(ops)) { Prelude.unitMap<BasicNumber>()["Wh"]!!.toUnitValue() }
        val oneWatt = QuantityValue(BasicOperations.pure(1.0), watt)
        val oneHour = QuantityValue(BasicOperations.pure(1.0), hour)
        val oneWattHour = QuantityValue(BasicOperations.pure(1.0), wattHour)

        // then
        with(QuantityValueOperations(ops)) {
            assertEquals((oneWatt * oneHour).absoluteScaleValue(), oneWattHour.absoluteScaleValue())
        }
    }

    @Test
    fun areaTime() {
        // given
        val squareMeter = with(ToValue(ops)) { Prelude.unitMap<BasicNumber>()["m2"]!!.toUnitValue() }
        val year = with(ToValue(ops)) { Prelude.unitMap<BasicNumber>()["year"]!!.toUnitValue() }
        val squareMeterYear = with(ToValue(ops)) { Prelude.unitMap<BasicNumber>()["m2a"]!!.toUnitValue() }
        val oneSquareMeter = QuantityValue(BasicOperations.pure(1.0), squareMeter)
        val oneYear = QuantityValue(BasicOperations.pure(1.0), year)
        val oneSquareMeterYear = QuantityValue(BasicOperations.pure(1.0), squareMeterYear)

        // then
        with(QuantityValueOperations(ops)) {
            assertEquals((oneSquareMeter * oneYear).absoluteScaleValue(), oneSquareMeterYear.absoluteScaleValue())
        }
    }

    @Test
    fun unitAlias_ShouldMergeSubList_ForReferenceUnits() {
        // Given

        // When
        val kg = Prelude.unitMap<BasicNumber>()["kg"]

        // Then
        assertNotNull(kg)
        assertEquals("mass", kg.dimension.toString())

    }

    @Test
    fun unitAlias_ShouldMergeSubList_ForAliasUnits() {
        // Given

        // When
        val tkm = Prelude.unitMap<BasicNumber>()["tkm"]

        // Then
        assertNotNull(tkm)
        assertEquals("length.mass", tkm.dimension.toString())

    }
}
