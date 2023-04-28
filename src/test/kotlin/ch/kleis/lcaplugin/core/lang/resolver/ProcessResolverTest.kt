package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
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
                name = "carrot_production",
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
                name = "salad_production",
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.salad),
                ),
                inputs = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
                ),
                biosphere = emptyList(),
            )
        )
        val processTemplates: Register<EProcessTemplate> = Register.from(
            mapOf(
                "carrot_production" to carrotProduction,
                "salad_production" to saladProduction,
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val resolver = ProcessResolver(symbolTable)

        // when
        val actual = resolver.resolveByProductName("carrot")

        // then
        assertEquals(carrotProduction, actual)
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
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(
                        EQuantityRef("q_carrot"), EConstrainedProduct(
                            EProductRef("carrot"),
                            None,
                        )
                    ),
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
                name = "salad_production",
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.salad),
                ),
                inputs = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
                ),
                biosphere = emptyList(),
            )
        )
        val processTemplates : Register<EProcessTemplate> = Register.from(
            mapOf(
                "carrot_production" to carrotProduction,
                "salad_production" to saladProduction,
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val resolver = ProcessResolver(symbolTable)

        // when
        val actual = resolver.resolveByProductName("carrot")

        // then
        assertEquals(carrotProduction, actual)
    }
}
