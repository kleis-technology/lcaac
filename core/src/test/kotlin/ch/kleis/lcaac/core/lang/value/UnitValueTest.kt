package ch.kleis.lcaac.core.lang.value

import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.fixture.DimensionFixture
import ch.kleis.lcaac.core.math.DoubleComparator
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UnitValueTest {

    val reference = UnitValue<BasicNumber>(UnitSymbol.of("kilo"), 1.25E-10, DimensionFixture.length)

    @Test
    fun testEquals_ShouldBeDifferent_WithDifferentDimension() {
        // Given
        val actual = UnitValue<BasicNumber>(UnitSymbol.of("kilo"), 1.25E-10, DimensionFixture.mass)

        // When + Then
        assertNotEquals(reference, actual)
        // No assertion on hashcode for different objects
    }

    @Test
    fun testEqualsAndHash_ShouldBeDifferent_WithDifferentScale() {
        // Given
        val actual =
            UnitValue<BasicNumber>(UnitSymbol.of("kilo"), 1.25E-10 * (1 + 2 * DoubleComparator.ACCEPTABLE_RELATIVE_ERROR), DimensionFixture.length)

        // When + Then
        assertNotEquals(reference, actual)
        // No assertion on hashcode for different objects
    }

    @Test
    fun testEqualsAndHash_ShouldEquals_WithCloseScaleAndSameDimension() {
        // Given
        val actual = UnitValue<BasicNumber>(
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
        val actual = UnitValue<BasicNumber>(
            UnitSymbol.of("meter"),
            1.25E-10 * (1 + 0.5 * DoubleComparator.ACCEPTABLE_RELATIVE_ERROR),
            DimensionFixture.length
        )

        // When + Then
        assertEquals(reference, actual)
        assertEquals(reference.hashCode(), actual.hashCode())
    }
}
