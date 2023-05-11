package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class ProductValueTest {
    @Test
    fun equals_whenReferenceUnitHaveSameDimension() {
        // given
        val a = ProductValueFixture.carrot.copy(referenceUnit = UnitValueFixture.kg)
        val b = ProductValueFixture.carrot.copy(referenceUnit = UnitValueFixture.ton)

        // then
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equals_whenReferenceUnitHaveDifferentDimensions() {
        // given
        val a = ProductValueFixture.carrot.copy(referenceUnit = UnitValueFixture.kg)
        val b = ProductValueFixture.carrot.copy(referenceUnit = UnitValueFixture.l)

        // then
        assertNotEquals(a, b)
    }

    @Test
    fun equals_whenExactCopy() {
        // given
        val a = ProductValueFixture.carrot
        val b = a.copy()

        // then
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
