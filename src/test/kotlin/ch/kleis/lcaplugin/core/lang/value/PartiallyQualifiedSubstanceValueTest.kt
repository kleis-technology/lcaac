package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.lang.fixture.PartiallyQualifiedSubstanceValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PartiallyQualifiedSubstanceValueTest {

    @Test
    fun equals_whenExactMatch() {
        // given
        val a = PartiallyQualifiedSubstanceValueFixture.propanol
        val b = a.copy()

        // then
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equals_whenSameDimension() {
        // given
        val a = PartiallyQualifiedSubstanceValueFixture.propanol.copy(referenceUnit = UnitValueFixture.kg())
        val b = PartiallyQualifiedSubstanceValueFixture.propanol.copy(referenceUnit = UnitValueFixture.ton())

        // then
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equals_whenDifferentDimensions() {
        // given
        val a = PartiallyQualifiedSubstanceValueFixture.propanol.copy(referenceUnit = UnitValueFixture.kg())
        val b = PartiallyQualifiedSubstanceValueFixture.propanol.copy(referenceUnit = UnitValueFixture.l())

        // then
        assertNotEquals(a, b)
    }
}
