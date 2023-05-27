package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class ProcessResolverTest {
    @Test
    fun resolve_withLabelMatching() {
        // given
        val carrotProductionBodyFR = EProcess(
            name = "carrot_production",
            labels = mapOf("geo" to EStringLiteral("FR")),
            products = listOf(
                ETechnoExchange(EQuantityRef("q_carrot"), ProductFixture.carrot),
            ),
            inputs = listOf(
                ETechnoExchange(EQuantityRef("q_water"), ProductFixture.water),
            ),
            biosphere = emptyList(),
        )
        val carrotProductionFR = EProcessTemplate(
            emptyMap(),
            emptyMap(),
            carrotProductionBodyFR,
        )
        val carrotProductionBodyUK = carrotProductionBodyFR.copy(
            labels = mapOf("geo" to EStringLiteral("UK")),
        )
        val carrotProductionUK = EProcessTemplate(
            emptyMap(),
            emptyMap(),
            carrotProductionBodyUK,
        )
        val processTemplates: Register<EProcessTemplate> = Register.from(
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
                labels = emptyMap(),
                products = listOf(
                    ETechnoExchange(EQuantityRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(EQuantityRef("q_water"), ProductFixture.water),
                ),
                biosphere = emptyList(),
            )
        )
        val processTemplates: Register<EProcessTemplate> = Register.from(
            mapOf(
                "carrot_production" to carrotProduction,
            )
        )
        val carrotSpec = ProductFixture.carrot.copy(
            name = "irrelevant_product",
            fromProcess = FromProcess("carrot_production", MatchLabels.EMPTY, emptyMap())
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
                labels = emptyMap(),
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
                labels = emptyMap(),
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
        val carrotSpec = ProductFixture.carrot.copy(
            fromProcess = FromProcess("carrot_production", MatchLabels.EMPTY, emptyMap())
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
                labels = emptyMap(),
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
                labels = emptyMap(),
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
}
