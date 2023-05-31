package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.fixture.DimensionFixture
import ch.kleis.lcaplugin.core.math.DoubleComparator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UnitValueTest {

    val reference = UnitValue(UnitSymbol.of("kilo"), 1.25E-10, DimensionFixture.length)
//    data class Scenario(val symbol:String, val scale:Double, val dimension: Dimension, )

    @Test
    fun testEquals_ShouldBeDifferent_WithDifferentDimension() {
        // Given
        val actual = UnitValue(UnitSymbol.of("kilo"), 1.25E-10, DimensionFixture.mass)

        // When + Then
        assertNotEquals(reference, actual)
        // No assertion on hashcode for different objects
    }

    @Test
    fun testEqualsAndHash_ShouldBeDifferent_WithDifferentScale() {
        // Given
        val actual =
            UnitValue(UnitSymbol.of("kilo"), 1.25E-10 * (1 + 2 * DoubleComparator.ACCEPTABLE_RELATIVE_ERROR), DimensionFixture.length)

        // When + Then
        assertNotEquals(reference, actual)
        // No assertion on hashcode for different objects
    }

    @Test
    fun testEqualsAndHash_ShouldEquals_WithCloseScaleAndSameDimension() {
        // Given
        val actual = UnitValue(
            UnitSymbol.of("kilo"),
            1.25E-10 * (1 + 0.5 * DoubleComparator.ACCEPTABLE_RELATIVE_ERROR),
            DimensionFixture.length
        )

        // When + Then
        assertEquals(reference, actual)
        assertEquals(reference.hashCode(), actual.hashCode())
    }

    @Test
    fun testEqualsAndHash_ShouldNotDependsOnSymbol() {
        // Given
        val actual = UnitValue(
            UnitSymbol.of("meter"),
            1.25E-10 * (1 + 0.5 * DoubleComparator.ACCEPTABLE_RELATIVE_ERROR),
            DimensionFixture.length
        )

        // When + Then
        assertEquals(reference, actual)
        assertEquals(reference.hashCode(), actual.hashCode())
    }
}
