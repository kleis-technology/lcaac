package ch.kleis.lcaplugin.core.lang.evaluator.linker

import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaplugin.core.lang.value.ConstraintValue
import ch.kleis.lcaplugin.core.lang.value.ProductValue
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test


class ProductPartialOrderTest {
    @Test
    fun leq_whenDifferentName_thenFalse() {
        // given
        val constraintPartialOrder = mockk<PartialOrder<ConstraintValue>>()
        every { constraintPartialOrder.leq(any(), any()) } returns true
        val poset = ProductPartialOrder(
            constraintPartialOrder = constraintPartialOrder
        )

        val referenceUnit = mockk<UnitValue>()
        val constraint = mockk<ConstraintValue>()
        val a = ProductValue("a", referenceUnit, constraint)
        val b = ProductValue("b", referenceUnit, constraint)


        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(false, actual)
    }

    @Test
    fun leq_whenSameNameAndDifferentReferenceUnit_thenFalse() {
        // given
        val constraintPartialOrder = mockk<PartialOrder<ConstraintValue>>()
        every { constraintPartialOrder.leq(any(), any()) } returns true
        val poset = ProductPartialOrder(
            constraintPartialOrder = constraintPartialOrder
        )

        val constraint = mockk<ConstraintValue>()
        val a = ProductValue("a", UnitValueFixture.kg, constraint)
        val b = ProductValue("a", UnitValueFixture.l, constraint)

        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(false, actual)
    }

    @Test
    fun leq_whenSameNameAndSameReferenceUnit_cpoReturnsFalse_thenFalse() {
        // given
        val constraintPartialOrder = mockk<PartialOrder<ConstraintValue>>()
        every { constraintPartialOrder.leq(any(), any()) } returns false
        val poset = ProductPartialOrder(
            constraintPartialOrder = constraintPartialOrder
        )

        val a = ProductValue("a", UnitValueFixture.kg, mockk())
        val b = ProductValue("a", UnitValueFixture.kg, mockk())

        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(false, actual)
    }

    @Test
    fun leq_whenSameNameAndSameReferenceUnit_cpoReturnsTrue_thenTrue() {
        // given
        val constraintPartialOrder = mockk<PartialOrder<ConstraintValue>>()
        every { constraintPartialOrder.leq(any(), any()) } returns true
        val poset = ProductPartialOrder(
            constraintPartialOrder = constraintPartialOrder
        )

        val a = ProductValue("a", UnitValueFixture.kg, mockk())
        val b = ProductValue("a", UnitValueFixture.kg, mockk())

        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(true, actual)
    }
}
