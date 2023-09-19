package ch.kleis.lcaac.core.lang.value

import ch.kleis.lcaac.core.lang.fixture.IndicatorValueFixture
import ch.kleis.lcaac.core.lang.fixture.UnitValueFixture
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class IndicatorValueTest {
    @Test
    fun equals_whenExactMatch() {
        // given
        val a = IndicatorValueFixture.climateChange
        val b = a.copy()

        // then
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equals_whenSameDimension() {
        // given
        val a = IndicatorValueFixture.climateChange.copy(referenceUnit = UnitValueFixture.kg())
        val b = IndicatorValueFixture.climateChange.copy(referenceUnit = UnitValueFixture.ton())

        // then
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equals_whenDifferentDimensions() {
        // given
        val a = IndicatorValueFixture.climateChange.copy(referenceUnit = UnitValueFixture.kg())
        val b = IndicatorValueFixture.climateChange.copy(referenceUnit = UnitValueFixture.l())

        // then
        assertNotEquals(a, b)
    }
}
