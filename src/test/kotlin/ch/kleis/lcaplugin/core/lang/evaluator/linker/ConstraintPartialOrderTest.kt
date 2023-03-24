package ch.kleis.lcaplugin.core.lang.evaluator.linker

import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.value.FromProcessRefValue
import ch.kleis.lcaplugin.core.lang.value.NoneValue
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test


class ConstraintPartialOrderTest {

    @Test
    fun leq_whenBothNone_thenTrue() {
        // given
        val poset = ConstraintPartialOrder()
        val a = NoneValue
        val b = NoneValue

        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(true, actual)
    }

    @Test
    fun leq_whenLeftIsNoneAndRightIsNotNone_thenFalse() {
        // given
        val poset = ConstraintPartialOrder()
        val a = NoneValue
        val b = mockk<FromProcessRefValue>()

        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(false, actual)
    }

    @Test
    fun leq_whenLeftIsNotNoneAndRightIsNone_thenTrue() {
        // given
        val poset = ConstraintPartialOrder()
        val a = mockk<FromProcessRefValue>()
        val b = NoneValue

        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(true, actual)
    }

    @Test
    fun leq_whenBothAreFromProcess_withDifferentProcessName_thenFalse() {
        // given
        val poset = ConstraintPartialOrder()
        val a = FromProcessRefValue("a", emptyMap())
        val b = FromProcessRefValue("b", emptyMap())

        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(false, actual)
    }

    @Test
    fun leq_whenBothAreFromProcess_withSameRef_withDifferentKeys_thenFalse() {
        // given
        val poset = ConstraintPartialOrder()
        val a = FromProcessRefValue(
            "p", mapOf(
                "x" to QuantityValueFixture.oneKilogram,
            )
        )
        val b = FromProcessRefValue(
            "p", mapOf(
                "y" to QuantityValueFixture.oneKilogram,
            )
        )

        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(false, actual)
    }

    @Test
    fun leq_whenBothAreFromProcess_withSameRef_withSameKeysAndDifferentValues_thenFalse() {
        // given
        val poset = ConstraintPartialOrder()
        val a = FromProcessRefValue(
            "p", mapOf(
                "x" to QuantityValueFixture.oneKilogram,
            )
        )
        val b = FromProcessRefValue(
            "p", mapOf(
                "x" to QuantityValueFixture.twoKilograms,
            )
        )

        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(false, actual)
    }

    @Test
    fun leq_whenBothAreFromProcess_withSameRef_leftIsSubsetOfRight_thenFalse() {
        // given
        val poset = ConstraintPartialOrder()
        val a = FromProcessRefValue(
            "p", mapOf(
                "x" to QuantityValueFixture.oneKilogram,
            )
        )
        val b = FromProcessRefValue(
            "p", mapOf(
                "x" to QuantityValueFixture.oneKilogram,
                "y" to QuantityValueFixture.oneLitre,
            )
        )

        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(false, actual)
    }

    @Test
    fun leq_whenBothAreFromProcess_withSameRef_rightIsSubsetOfLeft_thenFalse() {
        // given
        val poset = ConstraintPartialOrder()
        val a = FromProcessRefValue(
            "p", mapOf(
                "x" to QuantityValueFixture.oneKilogram,
                "y" to QuantityValueFixture.oneLitre,
            )
        )
        val b = FromProcessRefValue(
            "p", mapOf(
                "x" to QuantityValueFixture.oneKilogram,
            )
        )

        // when
        val actual = poset.leq(a, b)

        // then
        assertEquals(false, actual)
    }

}
