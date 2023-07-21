package ch.kleis.lcaplugin.ui.toolwindow

import org.junit.Test
import kotlin.test.assertEquals

class DisplayedNumberTest {
    @Test
    fun test_displayedNumber() {
        // given
        val value = 1.2345

        // when
        val actual = DisplayedNumber(value, nbSignificantDigits = 3)

        // then
        assertEquals(1, actual.getExponent())
        assertEquals(listOf(1, 2, 3), actual.getDigits())
    }

    @Test
    fun test_toString() {
        listOf(
            0.0 to "0",
            0.0001 to "1E-4",
            0.001 to "1E-3",
            0.01 to "0.01",
            1.0 to "1",
            10.0 to "10",
            100.0 to "100",
            1000.0 to "1E3",

            0.000999 to "1E-3",
            0.00999 to "0.01",
            0.0999 to "0.1",
            0.999 to "1",
            9.99 to "10",
            99.9 to "100",
            999.0 to "1E3",

            0.0123 to "1.23E-2",
            0.123 to "0.123",
            1.2345 to "1.23",
            12345.123 to "123E2",
            0.00123 to "1.23E-3",

            -0.0 to "0",
            -0.0001 to "-1E-4",
            -0.001 to "-1E-3",
            -0.01 to "-0.01",
            -1.0 to "-1",
            -10.0 to "-10",
            -100.0 to "-100",
            -1000.0 to "-1E3",

            -0.0123 to "-1.23E-2",
            -0.123 to "-0.123",
            -1.2345 to "-1.23",
            -12345.123 to "-123E2",
            -0.00123 to "-1.23E-3",
        ).forEach {
            val actual = DisplayedNumber(it.first).toString()
            assertEquals(it.second, actual)
        }
    }
}
