package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import org.junit.Assert.assertEquals
import org.junit.Test

class DimensionTest {
    @Test
    fun multiply() {
        // given
        val a = Dimension.of("a")
        val b = Dimension.of("b")

        // when
        val actual = a.multiply(b)

        // then
        val expected = Dimension(
            mapOf(
                "a" to 1.0,
                "b" to 1.0,
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun divide() {
        // given
        val a = Dimension.of("a")
        val b = Dimension.of("b")

        // when
        val actual = a.divide(b)

        // then
        val expected = Dimension(
            mapOf(
                "a" to 1.0,
                "b" to -1.0,
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun pow() {
        // given
        val a = Dimension.of("a")

        // when
        val actual = a.pow(2.0)

        // then
        val expected = Dimension(
            mapOf(
                "a" to 2.0,
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun toString_ShouldNotReturnPower_WhenPowerIsOne() {
        // Given
        val sut = Dimension.of("m")

        // When
        val result = sut.toString()

        // Then
        assertEquals("m", result)
    }


    @Test
    fun toString_ShouldReturnPowerWithoutDot_WhenPowerIsInteger() {
        // Given
        val sut = Dimension.of("m").pow(1234567890.0)

        // When
        val result = sut.toString()

        // Then
        assertEquals("m¹²³⁴⁵⁶⁷⁸⁹⁰", result)
    }

    @Test
    fun toString_ShouldReturnPowerWithoutDot_WhenPowerIsNegativeInteger() {
        // Given
        val sut = Dimension.of("m").pow(-1.0)

        // When
        val result = sut.toString()

        // Then
        assertEquals("m⁻¹", result)
    }

    @Test
    fun toString_ShouldFallBackToRawValue_WhenPowerIdDouble() {
        // Given
        val sut = Dimension.of("m").pow(2.5)

        // When
        val result = sut.toString()

        // Then
        assertEquals("m^[2.5]", result)
    }

    @Test
    fun toString_ShouldFallBackToRawValue_WhenPowerSimple() {
        // Given
        val sut = Dimension.of("m").pow(2E-15)

        // When
        val result = sut.toString()

        // Then
        assertEquals("m^[2.0E-15]", result)
    }


    @Test
    fun toString_ShouldReturnDefaultString_WhenPowerCompose() {
        // Given
        val sut = Dimension.of("m").multiply(Dimension.of("s").pow(-2.0))

        // When
        val result = sut.toString()

        // Then
        assertEquals("m.s⁻²", result)
    }

}
