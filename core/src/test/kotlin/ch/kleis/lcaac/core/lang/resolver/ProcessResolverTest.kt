package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.ProductFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ProcessResolverTest {
    @Test
    fun resolve_withLabelMatching() {
        // given
        val carrotProductionBodyFR = EProcess(
            name = "carrot_production",
            labels = mapOf("geo" to EStringLiteral("FR")),
            products = listOf(
                ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
            ),
            inputs = listOf(
                ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
            ),
        )
        val carrotProductionFR = EProcessTemplate(
            body = carrotProductionBodyFR,
        )
        val carrotProductionBodyUK = carrotProductionBodyFR.copy(
            labels = mapOf("geo" to EStringLiteral("UK")),
        )
        val carrotProductionUK = EProcessTemplate(
            body = carrotProductionBodyUK,
        )
        val processTemplates: Register<EProcessTemplate<BasicNumber>> = Register.from(
            mapOf(
                "carrot_production_FR" to carrotProductionFR,
                "carrot_production_UK" to carrotProductionUK,
            )
        )
        val carrotSpec = ProductFixture.carrot.copy(
            fromProcess = FromProcess(
                "carrot_production",
                MatchLabels(mapOf("geo" to EStringLiteral("UK"))),
                emptyMap(),
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val resolver = ProcessResolver(symbolTable)

        // when
        val actual = resolver.resolve(carrotSpec)

        // then
        assertEquals(carrotProductionUK, actual)
    }

    @Test
    fun resolve_whenFromProcess_andProductDoesNotMatch() {
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
                    ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
                ),
            )
        )
        val processTemplates: Register<EProcessTemplate<BasicNumber>> = Register.from(
            mapOf(
                "carrot_production" to carrotProduction,
            )
        )
        val carrotSpec = ProductFixture.carrot.copy(
            name = "irrelevant_product",
            fromProcess = FromProcess("carrot_production", MatchLabels(emptyMap()), emptyMap())
        )
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val resolver = ProcessResolver(symbolTable)

        // when/then
        val e = assertFailsWith(EvaluatorException::class) {
            resolver.resolve(carrotSpec)
        }
        assertEquals("no process 'carrot_production' providing 'irrelevant_product' found", e.message)
    }

    @Test
    fun resolve_whenFromProcess() {
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
                    ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
                ),
            )
        )
        val saladProduction = EProcessTemplate(
            body = EProcess(
                name = "salad_production",
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.salad),
                ),
                inputs = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
                ),
            )
        )
        val processTemplates: Register<EProcessTemplate<BasicNumber>> = Register.from(
            mapOf(
                "carrot_production" to carrotProduction,
                "salad_production" to saladProduction,
            )
        )
        val carrotSpec = ProductFixture.carrot.copy(
            fromProcess = FromProcess("carrot_production", MatchLabels(emptyMap()), emptyMap())
        )
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val resolver = ProcessResolver(symbolTable)

        // when
        val actual = resolver.resolve(carrotSpec)

        // then
        assertEquals(carrotProduction, actual)
    }

    @Test
    fun resolve_whenNameOnly_shouldFind() {
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
                    ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
                ),
            )
        )
        val saladProduction = EProcessTemplate(
            body = EProcess(
                name = "salad_production",
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.salad),
                ),
                inputs = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
                ),
            )
        )
        val processTemplates: Register<EProcessTemplate<BasicNumber>> = Register.from(
            mapOf(
                "carrot_production" to carrotProduction,
                "salad_production" to saladProduction,
            )
        )
        val carrotSpec = ProductFixture.carrot
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val resolver = ProcessResolver(symbolTable)

        // when
        val actual = resolver.resolve(carrotSpec)

        // then
        assertEquals(carrotProduction, actual)
    }

    @Test
    fun resolve_whenNameOnly_multipleMatch_shouldReturnNull() {
        // given
        val carrotProductionFR = EProcessTemplate(
            body = EProcess(
                name = "carrot_production",
                labels = mapOf("geo" to EStringLiteral("FR")),
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.water),
                ),
            )
        )
        val carrotProductionUK = EProcessTemplate(
            body = EProcess(
                name = "carrot_production",
                labels = mapOf("geo" to EStringLiteral("UK")),
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.water),
                ),
            )
        )
        val saladProduction = EProcessTemplate(
            body = EProcess(
                name = "salad_production",
                labels = emptyMap(),
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.salad),
                ),
                inputs = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
                ),
            )
        )
        val processTemplates: Register<EProcessTemplate<BasicNumber>> = Register.from(
            mapOf(
                "carrot_production_FR" to carrotProductionFR,
                "carrot_production_UK" to carrotProductionUK,
                "salad_production" to saladProduction,
            )
        )
        val carrotSpec = ProductFixture.carrot
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val resolver = ProcessResolver(symbolTable)

        // when
        val actual = resolver.resolve(carrotSpec)

        // then
        assertNull(actual)
    }
}
