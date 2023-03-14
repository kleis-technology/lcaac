package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessResolverTest {

    @Test
    fun findProcessesProducing_whenProductLiteral_shouldFind() {
        // given
        val carrotProduction = EProcessTemplate(
            params = mapOf(
                Pair("q_water", QuantityFixture.oneLitre)
            ),
            locals = mapOf(
                Pair("q_carrot", QuantityFixture.oneKilogram)
            ),
            body = EProcess(
                products = listOf(
                    ETechnoExchange(EQuantityRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(EQuantityRef("q_water"), ProductFixture.water),
                ),
                biosphere = emptyList(),
            )
        )
        val saladProduction = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            body = EProcess(
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.salad),
                ),
                inputs = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
                ),
                biosphere = emptyList(),
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = Register(mapOf(
                "carrot_production" to carrotProduction,
                "salad_production" to saladProduction,
            ))
        )
        val resolver = ProcessResolver(symbolTable)

        // when
        val actual = resolver.resolve("carrot")

        // then
        assertEquals(setOf("carrot_production" to carrotProduction), actual)
    }

    @Test
    fun findProcessesProducing_whenEProductRef_shouldFind() {
        // given
        val carrotProduction = EProcessTemplate(
            params = mapOf(
                Pair("q_water", QuantityFixture.oneLitre)
            ),
            locals = mapOf(
                Pair("q_carrot", QuantityFixture.oneKilogram)
            ),
            body = EProcess(
                products = listOf(
                    ETechnoExchange(EQuantityRef("q_carrot"), EProductRef("carrot")),
                ),
                inputs = listOf(
                    ETechnoExchange(EQuantityRef("q_water"), ProductFixture.water),
                ),
                biosphere = emptyList(),
            )
        )
        val saladProduction = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            body = EProcess(
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.salad),
                ),
                inputs = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
                ),
                biosphere = emptyList(),
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = Register(mapOf(
                "carrot_production" to carrotProduction,
                "salad_production" to saladProduction,
            ))
        )
        val resolver = ProcessResolver(symbolTable)

        // when
        val actual = resolver.resolve("carrot")

        // then
        assertEquals(setOf("carrot_production" to carrotProduction), actual)
    }
}
