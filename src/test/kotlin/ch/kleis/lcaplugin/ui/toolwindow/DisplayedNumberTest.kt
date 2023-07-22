package ch.kleis.lcaplugin.ui.toolwindow

import org.junit.Test
import kotlin.test.assertEquals

class DisplayedNumberTest {
    @Test
    fun test_positive_displayedNumber() {
        listOf(
            0.0012345 to -3 to "123",
            0.012345 to -2 to "123",
            0.12345 to -1 to "123",
            1.2345 to 0 to "123",
            12.345 to 1 to "123",
            123.45 to 2 to "123",
            1234.5 to 3 to "123",
            12345.0 to 4 to "123",

            0.0 to 0 to "000",
            0.0001 to -4 to "100",
            0.001 to -3 to "100",
            0.01 to -2 to "100",
            0.1 to -1 to "100",
            1.0 to 0 to "100",
            10.0 to 1 to "100",
            100.0 to 2 to "100",
            1000.0 to 3 to "100",

            0.000999 to -3 to "100",
            0.00999 to -2 to "100",
            0.0999 to -1 to "100",
            0.999 to 0 to "100",
            9.99 to 1 to "100",
            99.9 to 2 to "100",
            999.0 to 3 to "100",

            0.001001 to -3 to "100",
            0.01001 to -2 to "100",
            0.1001 to -1 to "100",
            1.001 to 0 to "100",
            10.01 to 1 to "100",
            100.1 to 2 to "100",
            1001.0 to 3 to "100",

            0.0002999 to -4 to "300",
            0.003999 to -3 to "400",
            0.04999 to -2 to "500",
            0.5999 to -1 to "600",
            6.999 to 0 to "700",
            79.90 to 1 to "800",
            899.9 to 2 to "900",
        ).forEach {
            val value = it.first.first
            val expectedPositionalExponent = it.first.second
            val expectedDigits = it.second

            val actual = DisplayedNumber(value)
            val actualPositionalExponent = actual.getPositionalExponent()
            val actualDigits = actual.getDigits().joinToString("")

            assertEquals(expectedPositionalExponent to expectedDigits, actualPositionalExponent to actualDigits, "value = $value")
        }
    }

    @Test
    fun test_negative_displayedNumber() {
        listOf(
            0.0012345 to -3 to "123",
            0.012345 to -2 to "123",
            0.12345 to -1 to "123",
            1.2345 to 0 to "123",
            12.345 to 1 to "123",
            123.45 to 2 to "123",
            1234.5 to 3 to "123",
            12345.0 to 4 to "123",

            0.0 to 0 to "000",
            0.0001 to -4 to "100",
            0.001 to -3 to "100",
            0.01 to -2 to "100",
            0.1 to -1 to "100",
            1.0 to 0 to "100",
            10.0 to 1 to "100",
            100.0 to 2 to "100",
            1000.0 to 3 to "100",

            0.000999 to -3 to "100",
            0.00999 to -2 to "100",
            0.0999 to -1 to "100",
            0.999 to 0 to "100",
            9.99 to 1 to "100",
            99.9 to 2 to "100",
            999.0 to 3 to "100",

            0.001001 to -3 to "100",
            0.01001 to -2 to "100",
            0.1001 to -1 to "100",
            1.001 to 0 to "100",
            10.01 to 1 to "100",
            100.1 to 2 to "100",
            1001.0 to 3 to "100",

            0.0002999 to -4 to "300",
            0.003999 to -3 to "400",
            0.04999 to -2 to "500",
            0.5999 to -1 to "600",
            6.999 to 0 to "700",
            79.90 to 1 to "800",
            899.9 to 2 to "900",
        ).forEach {
            val value = -it.first.first
            val expectedPositionalExponent = it.first.second
            val expectedDigits = it.second

            val actual = DisplayedNumber(value)
            val actualPositionalExponent = actual.getPositionalExponent()
            val actualDigits = actual.getDigits().joinToString("")

            assertEquals(expectedPositionalExponent to expectedDigits, actualPositionalExponent to actualDigits, "value = $value")
        }
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

            0.001001 to "1E-3",
            0.01001 to "0.01",
            0.1001 to "0.1",
            1.001 to "1",
            10.01 to "10",
            100.1 to "100",
            1001.0 to "1E3",

            0.003999 to "4E-3",
            0.04999 to "0.05",
            0.5999 to "0.6",
            6.999 to "7",
            79.90 to "80",
            899.9 to "900",
            999.0 to "1E3",

            0.00123 to "1.23E-3",
            0.0123 to "0.0123",
            0.123 to "0.123",
            1.2345 to "1.23",
            12.345 to "12.3",
            123.45 to "123",
            1234.5 to "1.23E3",
            12345.123 to "1.23E4",

            -0.0 to "0",
            -0.0001 to "-1E-4",
            -0.001 to "-1E-3",
            -0.01 to "-0.01",
            -1.0 to "-1",
            -10.0 to "-10",
            -100.0 to "-100",
            -1000.0 to "-1E3",

            -0.0123 to "-0.0123",
            -0.123 to "-0.123",
            -1.2345 to "-1.23",
            -12345.123 to "-1.23E4",
            -0.00123 to "-1.23E-3",
        ).forEach {
            val actual = DisplayedNumber(it.first).toString()
            assertEquals(it.second, actual)
        }
    }
}
