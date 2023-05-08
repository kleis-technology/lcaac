package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import org.junit.Assert.*
import org.junit.Test

class RegisterTest {
    @Test
    fun set_and_get() {
        // given
        val key = "abc.x"
        val a = EQuantityRef("a")
        val register = Register.empty<QuantityExpression>()

        // when
        val actual = register.plus(listOf(key to a))

        // then
        assertEquals(a, actual[key])
    }

    @Test
    fun set_whenDuplicate_atOnce() {
        // given
        val key = "abc.x"
        val a = EQuantityRef("a")
        val b = EQuantityRef("b")
        val register = Register.empty<QuantityExpression>()

        // when
        try {
            register.plus(listOf(key to a, key to b))
            fail("should have thrown EvaluatorException")
        } catch (e: EvaluatorException) {
            assertEquals("[abc.x] are already bound", e.message)
        }
    }

    @Test
    fun set_whenDuplicate_successive() {
        // given
        val key = "abc.x"
        val a = EQuantityRef("a")
        val b = EQuantityRef("b")
        val register = Register.empty<QuantityExpression>()

        // when
        try {
            val r = register.plus(listOf(key to a))
            r.plus(listOf(key to b))
            fail("should have thrown EvaluatorException")
        } catch (e: EvaluatorException) {
            assertEquals("[$key] are already bound", e.message)
        }
    }

    @Test
    fun equals_whenEquals_thenTrue() {
        // given
        val r1 = Register.empty<Double>().plus(listOf("a" to 1.0, "b" to 2.0))
        val r2 = Register.empty<Double>().plus(listOf("a" to 1.0, "b" to 2.0))

        // then
        assertEquals(r1, r2)
    }

    @Test
    fun equals_whenSameKeysDifferentValue_thenFalse() {
        // given
        val r1 = Register.empty<Double>().plus(listOf("a" to 1.0, "b" to 2.0))
        val r2 = Register.empty<Double>().plus(listOf("a" to 1.0, "b" to 3.0))

        // then
        assertNotEquals(r1, r2)
    }

    @Test
    fun equals_whenDifferentKeys_thenFalse() {
        // given
        val r1 = Register.empty<Double>().plus(listOf("a" to 1.0, "b" to 2.0))
        val r2 = Register.empty<Double>().plus(listOf("a" to 1.0, "c" to 2.0))

        // then
        assertNotEquals(r1, r2)
    }

    @Test
    fun getEntries_ShouldReturnIndex() {
        // Given
        val key = "abc.x"
        val a = EQuantityRef("a")

        val key2 = "abc.y"
        val b = EQuantityRef("b")
        val sut = Register.empty<EQuantityRef>().plus(listOf(key to a, key2 to b))

        // When
        val entries = sut.getEntries(EQuantityRef.name)

        // Then
        assertEquals(a, entries["a"])
    }

    @Test
    fun getEntries_ShouldReturnAnError_WhenContainsDuplicates() {
        // Given
        val keyA = "abc.a"
        val keyB = "abc.b"
        val kg = EQuantityRef("kg")
        val a = EUnitAlias("a", kg)
        val b = EUnitAlias("b", kg)
        val sut = Register.empty<EUnitAlias>()
                .plus(listOf(keyA to a, keyB to b))

        // When
        try {
            Index(
                    sut,
                    object : IndexKeySerializer<String> {
                        override fun serialize(key: String): String = key
                    },
                    EUnitAlias.aliasFor compose QuantityExpression.eQuantityRef.name
            )
            fail("should have thrown EvaluatorException")
        } catch (e: EvaluatorException) {
            // Then
            assertEquals("kg is already bound", e.message)
        }
    }
}
