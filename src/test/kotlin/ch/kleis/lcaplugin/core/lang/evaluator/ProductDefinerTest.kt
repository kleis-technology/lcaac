package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.lang.ClassCastException

class ProductDefinerTest {

    @Test
    fun complete_exchange() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val a = EVar("a")
        val exchange = EExchange(EQuantity(1.0, kg), a)
        val productDefiner = ProductDefiner()
        // when
        val actual = productDefiner.complete(exchange)
        // then
        val expected = EExchange(EQuantity(1.0, kg), EProduct("a", kg))
        assertEquals(expected, actual)
    }

    @Test
    fun complete_process() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val a = EVar("a")
        val exchange = EExchange(EQuantity(1.0, kg), a)
        val process = EProcess(listOf(exchange))
        val productDefiner = ProductDefiner()
        // when
        val actual = productDefiner.complete(process)
        // then
        val expected = EProcess(
            listOf(
                EExchange(
                    EQuantity(1.0, kg), EProduct("a", kg)
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun complete_exchangeWithVariableUnit() {
        // given
        val kg = EVar("kg")
        val a = EVar("a")
        val exchange = EExchange(EQuantity(1.0, kg), a)
        val productDefiner = ProductDefiner()
        // when + then
        try {
            productDefiner.complete(exchange)
            fail("Should have drown")
        } catch (e: ClassCastException) {
            //success
        }
    }

    @Test
    fun complete_let() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val a = EVar("a")
        val let = ELet(
            mapOf(Pair("x", EExchange(EQuantity(1.0, kg), EVar("y")))),
            EExchange(EQuantity(1.0, kg), a)
        )
        val productDefiner = ProductDefiner()
        // when
        val actual = productDefiner.complete(let)
        // then
        val expected = ELet(
            mapOf(Pair("x", EExchange(EQuantity(1.0, kg), EVar("y")))),
            EExchange(EQuantity(1.0, kg), EProduct("a", kg))
        )
        assertEquals(expected, actual)
    }
}