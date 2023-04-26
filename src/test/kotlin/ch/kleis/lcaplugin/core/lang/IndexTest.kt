package ch.kleis.lcaplugin.core.lang

import arrow.optics.Every
import arrow.optics.PEvery
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.Merge
import ch.kleis.lcaplugin.core.lang.expression.optics.everyQuantityRefInQuantityExpression
import org.junit.Assert.*
import org.junit.Test

class IndexTest {
    @Test
    fun set_and_get() {
        // given
        val key = "abc.x"
        val a = EQuantityRef("a")

        val key2 = "abc.y"
        val b = EQuantityRef("b")
        val register = Register.empty<EQuantityRef>().plus(listOf(key to a, key2 to b))
        val index = Index(register, EQuantityRef.name)

        // when
        val actual = index["a"]

        // then
        assertEquals(a, actual)
    }


    @Test
    fun set_whenDuplicate_atOnce() {
        // given
        val keyA = "abc.a"
        val keyB = "abc.b"
        val kg = EQuantityRef("kg")
        val a = EUnitAlias("a", kg)
        val b = EUnitAlias("b", kg)
        val register = Register.empty<EUnitAlias>()
            .plus(listOf(keyA to a, keyB to b))

        // when
        try {
            Index(register, EUnitAlias.aliasFor compose QuantityExpression.eQuantityRef.name)
            fail("should have thrown EvaluatorException")
        } catch (e: EvaluatorException) {
            assertEquals("kg is already bound", e.message)
        }
    }


}