package ch.kleis.lcaplugin.ui.toolwindow

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters


@RunWith(Parameterized::class)
class DisplayedNumberTest(
    private val value: Double,
    private val expected: String,
) {
    companion object {
        @Parameters
        @JvmStatic
        fun getDisplayedStrings(): Collection<Array<Any>> {
            return listOf(
                arrayOf(0.0, "0"),
                arrayOf(0.0001, "1E-4"),
                arrayOf(0.001, "1E-3"),
                arrayOf(0.01, "0.01"),
                arrayOf(0.1, "0.1"),
                arrayOf(1.0, "1"),
                arrayOf(10.0, "10"),
                arrayOf(100.0, "100"),
                arrayOf(1000.0, "1E3"),

                arrayOf(0.000999, "1E-3"),
                arrayOf(0.00999, "0.01"),
                arrayOf(0.0999, "0.1"),
                arrayOf(0.999, "1"),
                arrayOf(9.99, "10"),
                arrayOf(99.9, "100"),
                arrayOf(999.0, "1E3"),

                arrayOf(0.00123, "1.23E-3"),
                arrayOf(0.0123, "0.0123"),
                arrayOf(0.123, "0.123"),
                arrayOf(1.2345, "1.23"),
                arrayOf(12.345, "12.3"),
                arrayOf(123.45, "123"),
                arrayOf(1234.5, "1.23E3"),
                arrayOf(12345.123, "1.23E4"),

                arrayOf(-0.0, "0"),
                arrayOf(-0.00123, "-1.23E-3"),
                arrayOf(-0.0123, "-0.0123"),
                arrayOf(-0.123, "-0.123"),
                arrayOf(-1.2345, "-1.23"),
                arrayOf(-12.345, "-12.3"),
                arrayOf(-123.45, "-123"),
                arrayOf(-1234.5, "-1.23E3"),
                arrayOf(-12345.123, "-1.23E4"),
            )
        }
    }

    @Test
    fun run() {
        // when
        val actual = DisplayedNumber(value).toString()

        // then
        assertEquals(expected, actual)
    }
}
