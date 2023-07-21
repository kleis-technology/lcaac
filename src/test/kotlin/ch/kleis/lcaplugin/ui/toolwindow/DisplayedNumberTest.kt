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
            0.0123 to "1.23E-2",
            0.123 to "0.123",
            1.0 to "1",
            1.2345 to "1.23",
            12345.123 to "123E2",
            0.00123 to "1.23E-3",

            -0.0 to "0",
            -0.0123 to "-1.23E-2",
            -0.123 to "-0.123",
            -1.0 to "-1",
            -1.2345 to "-1.23",
            -12345.123 to "-123E2",
            -0.00123 to "-1.23E-3",
        ).forEach {
            val actual = DisplayedNumber(it.first).toString()
            assertEquals(it.second, actual)
        }
    }
}
