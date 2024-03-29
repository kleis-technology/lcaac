package ch.kleis.lcaac.core.lang

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.lang.register.Register
import ch.kleis.lcaac.core.lang.register.RegisterException
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class RegisterTest {
    @Test
    fun override() {
        // given
        val key = DataKey("abc.x")
        val a = EDataRef<BasicNumber>("a")
        val b = EDataRef<BasicNumber>("b")
        val register = DataRegister<BasicNumber>().plus(mapOf(
            key to a
        ))

        // when
        val actual = register.override(key, b)[key]

        // then
        assertEquals(b, actual)
    }

    @Test
    fun set_and_get() {
        // given
        val key = DataKey("abc.x")
        val a = EDataRef<BasicNumber>("a")
        val register = DataRegister<BasicNumber>()

        // when
        val actual = register.plus(listOf(key to a))

        // then
        assertEquals(a, actual[key])
    }

    @Test
    fun set_whenDuplicate_atOnce() {
        // given
        val key = DataKey("abc.x")
        val a = EDataRef<BasicNumber>("a")
        val b = EDataRef<BasicNumber>("b")
        val register = DataRegister<BasicNumber>()
        val duplicateKeys = listOf(
            key to a,
            key to b
        )


        // When + Then
        val e = assertFailsWith(RegisterException::class, null) { register.plus(duplicateKeys) }
        assertEquals("[abc.x] is already bound", e.message)
    }

    @Test
    fun set_whenDuplicate_successive() {
        // given
        val key = DataKey("abc.x")
        val a = EDataRef<BasicNumber>("a")
        val b = EDataRef<BasicNumber>("b")
        val register = DataRegister<BasicNumber>().plus(listOf(key to a))
        val message = "[$key] is already bound"
        val duplicateKey = listOf(key to b)

        // When + Then
        val e = assertFailsWith(RegisterException::class, null) { register.plus(duplicateKey) }
        assertEquals(message, e.message)
    }

    @Test
    fun equals_whenEquals_thenTrue() {
        // given
        val r1 = Register.empty<String, Double>().plus(listOf("a" to 1.0, "b" to 2.0))
        val r2 = Register.empty<String, Double>().plus(listOf("a" to 1.0, "b" to 2.0))

        // then
        assertEquals(r1, r2)
    }

    @Test
    fun equals_whenSameKeysDifferentValue_thenFalse() {
        // given
        val r1 = Register.empty<String, Double>().plus(listOf("a" to 1.0, "b" to 2.0))
        val r2 = Register.empty<String, Double>().plus(listOf("a" to 1.0, "b" to 3.0))

        // then
        assertNotEquals(r1, r2)
    }

    @Test
    fun equals_whenDifferentKeys_thenFalse() {
        // given
        val r1 = Register.empty<String, Double>().plus(listOf("a" to 1.0, "b" to 2.0))
        val r2 = Register.empty<String, Double>().plus(listOf("a" to 1.0, "c" to 2.0))

        // then
        assertNotEquals(r1, r2)
    }

    @Test
    fun getEntries_ShouldReturnIndex() {
        // Given
        val key = "abc.x"
        val a = EDataRef<BasicNumber>("a")

        val key2 = "abc.y"
        val b = EDataRef<BasicNumber>("b")
        val sut = Register.empty<String, EDataRef<BasicNumber>>().plus(listOf(key to a, key2 to b))

        // When
        val actual = sut.getEntries(EDataRef.name())["a"]!!

        // Then
        assertEquals(1, actual.size)
        assertEquals(a, actual[0])
    }

    @Test
    fun getEntries_whenContainsDuplicates_shouldReturnList() {
        // given
        val keyA = "abc.a"
        val keyB = "abc.b"
        val kg = EDataRef<BasicNumber>("kg")
        val a = EUnitAlias("a", kg)
        val b = EUnitAlias("b", kg)
        val optics =
            EUnitAlias.aliasFor<BasicNumber>() compose
                DataExpression.eDataRef<BasicNumber>().name()
        val register = Register.empty<String, EUnitAlias<BasicNumber>>()
            .plus(listOf(keyA to a, keyB to b))


        // when
        val actual = register.getEntries(optics)

        // then
        assertEquals(1, actual.size)
        assertEquals(listOf(a, b), actual["kg"]!!)
    }
}
