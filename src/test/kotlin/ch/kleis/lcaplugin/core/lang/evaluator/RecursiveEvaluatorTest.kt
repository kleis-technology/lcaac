package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.ProcessValue
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.TechnoExchangeValue
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class RecursiveEvaluatorTest {
    @Test
    fun eval_withImplicitProcessResolution_thenCorrectSystem() {
        // given
        val symbolTable = SymbolTable(
            processTemplates = Register(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction
                )
            )
        )
        val expression = EProcessTemplate(
            emptyMap(),
            emptyMap(),
            EProcess(
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(UnconstrainedProductFixture.salad, None),
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(
                            EProductRef("carrot"),
                            None,
                        )
                    )
                ),
                biosphere = emptyList()
            )
        )
        val recursiveEvaluator = RecursiveEvaluator(symbolTable)

        // when
        val actual = recursiveEvaluator.eval(expression).processes.toSet()

        // then
        val expected = setOf(
            ProcessValue(
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot
                    )
                ),
                inputs = listOf(TechnoExchangeValue(QuantityValueFixture.oneLitre, ProductValueFixture.water)),
                biosphere = emptyList(),
            ),
            ProcessValue(
                products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, ProductValueFixture.salad)),
                inputs = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, ProductValueFixture.carrot)),
                biosphere = emptyList(),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_withImplicitProcessResolution_whenMoreThanOneProcess_shouldThrow() {
        // given
        val symbolTable = SymbolTable(
            processTemplates = Register(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction,
                    "carrot_production_bis" to TemplateFixture.carrotProduction,
                )
            ),
        )
        val expression = EProcessTemplate(
            emptyMap(),
            emptyMap(),
            EProcess(
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(UnconstrainedProductFixture.salad, None),
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(
                            EProductRef("carrot"),
                            None,
                        )
                    )
                ),
                biosphere = emptyList()
            )
        )
        val recursiveEvaluator = RecursiveEvaluator(symbolTable)

        // when/then
        try {
            recursiveEvaluator.eval(expression).processes.toSet()
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals(
                "more than one process produces carrot : [carrot_production, carrot_production_bis]",
                e.message
            )
        }
    }

    @Test
    fun eval_withExplicitProcessResolution_whenMoreThanOneProcess_shouldThrow() {
        // given
        val symbolTable = SymbolTable(
            processTemplates = Register(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction,
                    "carrot_production_bis" to TemplateFixture.carrotProduction,
                )
            )
        )
        val expression = EProcessTemplate(
            emptyMap(),
            emptyMap(),
            EProcess(
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(UnconstrainedProductFixture.salad, None),
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(
                            EProductRef("carrot"),
                            FromProcessRef(
                                ETemplateRef("carrot_production"),
                                emptyMap(),
                            ),
                        )
                    )
                ),
                biosphere = emptyList()
            )
        )
        val recursiveEvaluator = RecursiveEvaluator(symbolTable)

        // when/then
        try {
            recursiveEvaluator.eval(expression).processes.toSet()
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals(
                "more than one process produces carrot : [carrot_production, carrot_production_bis]",
                e.message
            )
        }
    }

    @Test
    fun eval_whenExistsFromProcessRef_thenCorrectSystem() {
        // given
        val symbolTable = SymbolTable(
            processTemplates = Register(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction
                )
            )
        )
        val expression = EProcessTemplate(
            emptyMap(),
            emptyMap(),
            EProcess(
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(UnconstrainedProductFixture.salad, None),
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(
                            EProductRef("carrot"),
                            FromProcessRef(
                                ETemplateRef("carrot_production"),
                                mapOf("q_water" to QuantityFixture.twoLitres),
                            )
                        )
                    )
                ),
                biosphere = emptyList()
            )
        )
        val recursiveEvaluator = RecursiveEvaluator(symbolTable)

        // when
        val actual = recursiveEvaluator.eval(expression).processes.toSet()

        // then
        val expected = setOf(
            ProcessValue(
                products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, ProductValueFixture.salad)),
                inputs = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, ProductValueFixture.carrot)),
                biosphere = emptyList(),
            ),
            ProcessValue(
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot
                    )
                ),
                inputs = listOf(TechnoExchangeValue(QuantityValueFixture.twoLitres, ProductValueFixture.water)),
                biosphere = emptyList(),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenProductDoesNotMatchProcess_shouldThrow() {
        // given
        val symbolTable = SymbolTable(
            processTemplates = Register(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction
                )
            )
        )
        val expression = EProcessTemplate(
            emptyMap(),
            emptyMap(),
            EProcess(
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(UnconstrainedProductFixture.salad, None),
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(
                            EProductRef("irrelevant_product"),
                            FromProcessRef(
                                ETemplateRef("carrot_production"),
                                mapOf("q_water" to QuantityFixture.twoLitres),
                            )
                        )
                    )
                ),
                biosphere = emptyList()
            )
        )
        val recursiveEvaluator = RecursiveEvaluator(symbolTable)

        // when/then
        try {
            recursiveEvaluator.eval(expression)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals("irrelevant_product does not match any product of carrot_production", e.message)
        }
    }

    @Test
    fun eval_whenNonEmptyBiosphere_thenIncludeSubstanceCharacterization() {
        // given
        val symbolTable = SymbolTable(
            substanceCharacterizations = Register(
                mapOf(
                    "propanol" to SubstanceCharacterizationFixture.propanolCharacterization,
                )
            ),
            substances = Register(
                mapOf(
                    "propanol" to SubstanceFixture.propanol,
                )
            )
        )
        val expression = EProcessTemplate(
            emptyMap(),
            emptyMap(),
            EProcess(
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(UnconstrainedProductFixture.salad, None),
                    )
                ),
                inputs = emptyList(),
                biosphere = listOf(
                    EBioExchange(QuantityFixture.oneKilogram, ESubstanceRef("propanol"))
                )
            )
        )
        val recursiveEvaluator = RecursiveEvaluator(symbolTable)

        // when
        val actual = recursiveEvaluator.eval(expression).substanceCharacterizations.toSet()

        // then
        val expected = setOf(
            SubstanceCharacterizationValueFixture.propanolCharacterization
        )
        assertEquals(expected, actual)
    }
}
