package ch.kleis.lcaplugin.core.lang.evaluator.linker

import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaplugin.core.lang.value.*
import com.intellij.openapi.ui.naturalSorted
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test


class ProductPartialOrderTest {
    @Test
    fun moreConcreteThanOrEqualTo_whenDifferentName_thenFalse() {
        // given
        val poset = ProductPartialOrder()

        val referenceUnit = mockk<UnitValue>()
        val constraint = mockk<ConstraintValue>()
        val a = ProductValue("a", referenceUnit, constraint)
        val b = ProductValue("b", referenceUnit, constraint)


        // when
        val actual = poset.moreConcreteThanOrEqualTo(a, b)

        // then
        assertEquals(false, actual)
    }

    @Test
    fun moreConcreteThanOrEqualTo_whenSameNameAndDifferentReferenceUnit_thenFalse() {
        // given
        val poset = ProductPartialOrder()

        val a = ProductValue("a", UnitValueFixture.kg, mockk())
        val b = ProductValue("a", UnitValueFixture.l, mockk())

        // when
        val actual = poset.moreConcreteThanOrEqualTo(a, b)

        // then
        assertEquals(false, actual)
    }

    @Test
    fun moreConcreteThanOrEqualTo_whenSameNameAndSameReferenceUnit_cpoReturnsFalse_thenFalse() {
        // given
        val poset = ProductPartialOrder()

        val a = ProductValue("a", UnitValueFixture.kg, NoneValue)
        val b = ProductValue("a", UnitValueFixture.kg, FromProcessRefValue("a_prod", emptyMap()))

        // when
        val actual = poset.moreConcreteThanOrEqualTo(a, b)

        // then
        assertEquals(false, actual)
    }

    @Test
    fun moreConcreteThanOrEqualTo_whenSameNameAndSameReferenceUnit_cpoReturnsTrue_thenTrue() {
        // given
        val poset = ProductPartialOrder()

        val constraint = NoneValue
        val a = ProductValue("a", UnitValueFixture.kg, constraint)
        val b = ProductValue("a", UnitValueFixture.kg, constraint)

        // when
        val actual = poset.moreConcreteThanOrEqualTo(a, b)

        // then
        assertEquals(true, actual)
    }

    @Test
    fun mostConcreteElementsOf_shouldReturnTheMostConcreteElements() {
        // given
        val poset = ProductPartialOrder()
        val a = ProductValue("a", UnitValueFixture.kg, NoneValue)
        val concreteA = ProductValue("a", UnitValueFixture.kg, FromProcessRefValue("a_prod", emptyMap()))
        val b = ProductValue("b", UnitValueFixture.kg, NoneValue)
        val elements = listOf(a, concreteA, b)

        // when
        val actual = poset.mostConcreteElementsOf(elements).naturalSorted()

        // then
        val expected = listOf(
            concreteA, b
        ).naturalSorted()
        assertEquals(expected, actual)
    }
}
