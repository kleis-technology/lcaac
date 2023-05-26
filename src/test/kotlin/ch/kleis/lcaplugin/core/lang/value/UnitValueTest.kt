package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.lang.fixture.DimensionFixture
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UnitValueTest {

    val reference = UnitValue("kilo", 1.25E-10, DimensionFixture.length)
//    data class Scenario(val symbol:String, val scale:Double, val dimension: Dimension, )

    @Test
    fun testEquals_ShouldBeDifferent_WithDifferentDimension() {
        // Given
        val actual = UnitValue("kilo", 1.25E-10, DimensionFixture.mass)

        // When + Then
        assertNotEquals(reference, actual)
        // No assertion on hashcode for different objects
    }

    @Test
    fun testEqualsAndHash_ShouldBeDifferent_WithDifferentScale() {
        // Given
        val actual = UnitValue("kilo", 1.25E-10 + 1E-20, DimensionFixture.length)

        // When + Then
        assertNotEquals(reference, actual)
        // No assertion on hashcode for different objects
    }

    @Test
    fun testEqualsAndHash_ShouldEquals_WithCloseScaleAndSameDimension() {
        // Given
        val actual = UnitValue("kilo", 1.25E-10 + 1E-21, DimensionFixture.length)

        // When + Then
        assertEquals(reference, actual)
        assertEquals(reference.hashCode(), actual.hashCode())
    }

    @Test
    fun testEqualsAndHash_ShouldEquals_WithCloseScaleAndSameDimensionWhenScaleZero_EvenItsNotSupposedToExist() {
        // Given
        val zero = UnitValue("kilo", 0.0, DimensionFixture.length)
        val actual = UnitValue("kilo", 9E-12, DimensionFixture.length)

        // When + Then
        assertEquals(zero, actual)
        assertEquals(zero.hashCode(), actual.hashCode())
    }

    @Test
    fun testEqualsAndHash_ShouldNotsEquals_WithDifferentScaleAndSameDimensionWhenOtherScaleIsZero_EvenItsNotSupposedToExist() {
        // Given
        val zero = UnitValue("kilo", 0.0, DimensionFixture.length)
        val actual = UnitValue("kilo", 1.1E-11, DimensionFixture.length)

        // When + Then
        assertNotEquals(zero, actual)
    }

    @Test
    fun testEqualsAndHash_ShouldNotDependsOnSymbol() {
        // Given
        val actual = UnitValue("meter", 1.25E-10 + 1E-21, DimensionFixture.length)

        // When + Then
        assertEquals(reference, actual)
        assertEquals(reference.hashCode(), actual.hashCode())
    }
}