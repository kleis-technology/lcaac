package ch.kleis.lcaac.core.lang.dimension

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class DimensionTest {

    @Test
    fun test_toString_whenNone() {
        // given
        val dimension = Dimension.None

        // when
        val actual = "$dimension"

        // then
        assertEquals("none", actual)
    }

    @Test
    fun test_toString_whenSimpleDim() {
        // given
        val dimension = Dimension.of("something")

        // when
        val actual = "$dimension"

        // then
        assertEquals("something", actual)
    }

    @Test
    fun test_toString_whenSmallPower() {
        // given
        val dimension = Dimension.of("something", 2)

        // when
        val actual = "$dimension"

        // then
        assertEquals("something²", actual)
    }
}
