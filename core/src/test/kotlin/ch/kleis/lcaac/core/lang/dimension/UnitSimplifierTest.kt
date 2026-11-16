package ch.kleis.lcaac.core.lang.dimension

import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UnitSimplifierTest {
    private val energy = Dimension.of("energy")
    private val mass = Dimension.of("mass")
    private val kWh = UnitValue<BasicNumber>(UnitSymbol.of("kWh"), 3.6e6, energy)
    private val kg = UnitValue<BasicNumber>(UnitSymbol.of("kg"), 1.0, mass)
    private val u = UnitValue<BasicNumber>(UnitSymbol.of("u"), 1.0, Dimension.None)
    private val percent = UnitValue<BasicNumber>(UnitSymbol.of("percent"), 0.01, Dimension.None)
    private val simplifier = UnitSimplifier(BasicOperations, listOf(kWh, kg, u, percent).associateBy { it.symbol.toString() })

    private fun quantity(amount: Double, components: Map<String, Double>, scale: Double, dimension: Dimension) =
        QuantityValue(BasicNumber(amount), UnitValue(UnitSymbol(components), scale, dimension))

    @Test
    fun simplify_shouldRemoveDimensionlessFactors() {
        // given: 2 kWh * 3 u / 80 percent * 110 percent, as computed
        val energyQuantity = quantity(0.0825, mapOf("kWh" to 1.0, "u" to 2.0, "percent" to -1.0), 3.6e6 / 0.01, energy)

        // when
        val actual = assertNotNull(simplifier.simplify(energyQuantity))

        // then
        assertEquals(kWh.symbol, actual.unit.symbol)
        assertEquals(8.25, actual.amount.value, 1e-9)
    }

    @Test
    fun simplify_shouldKeepTheValue() {
        val actual = assertNotNull(simplifier.simplify(quantity(15.0, mapOf("kg" to 1.0, "percent" to 1.0), 0.01, mass)))
        assertEquals("kg", actual.unit.symbol.toString())
        assertEquals(0.15, actual.amount.value, 1e-9)
    }

    @Test
    fun simplify_whenOnlyDimensionlessUnits_shouldGiveUnits() {
        val actual = assertNotNull(simplifier.simplify(quantity(5.0, mapOf("u" to 1.0, "percent" to 1.0), 0.01, Dimension.None)))
        assertEquals("u", actual.unit.symbol.toString())
        assertEquals(0.05, actual.amount.value, 1e-9)
    }

    @Test
    fun simplify_whenNothingToSimplify_shouldBeNull() {
        assertNull(simplifier.simplify(QuantityValue(BasicNumber(2.0), kg)))
        assertNull(simplifier.simplify(quantity(2.0, mapOf("kg" to 2.0), 1.0, mass.pow(2.0))))
        assertNull(simplifier.simplify(quantity(2.0, mapOf("kg" to 1.0, "kWh" to 1.0), 3.6e6, mass.multiply(energy))))
    }

    @Test
    fun simplify_whenUnknownUnit_shouldBeNull() {
        assertNull(simplifier.simplify(quantity(2.0, mapOf("kg" to 1.0, "foo" to 1.0), 1.0, mass)))
    }
}
