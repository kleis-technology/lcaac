package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.*
import com.intellij.openapi.ui.naturalSorted
import org.junit.Assert.*
import org.junit.Test

class EvaluatorTest {
    @Test
    fun eval_whenTwoInstancesOfSameTemplate_thenDifferentProduct() {
        // given
        val template = TemplateFixture.carrotProduction
        val i1 = EInstance(template, mapOf("q_water" to QuantityFixture.oneLitre))
        val i2 = EInstance(template, mapOf("q_water" to QuantityFixture.twoLitres))
        val symbolTable = SymbolTable.empty()
        val recursiveEvaluator = Evaluator(symbolTable)

        // when
        val p1 = recursiveEvaluator.eval(i1).processes.first().products.first().product
        val p2 = recursiveEvaluator.eval(i2).processes.first().products.first().product

        // then
        assertEquals(p1.name, p2.name)
        assertEquals(p1.referenceUnit, p2.referenceUnit)
        assertNotEquals(p1, p2)
    }

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
                name = "salad_production",
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
        val recursiveEvaluator = Evaluator(symbolTable)

        // when
        val actual = recursiveEvaluator.eval(expression).processes.naturalSorted()

        // then
        val expected = setOf(
            ProcessValue(
                name = "carrot_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withConstraint(
                            FromProcessRefValue(
                                "carrot_production",
                                mapOf(
                                    "q_water" to QuantityValueFixture.oneLitre
                                ),
                                ConstraintFlag.IS_DEFAULT,
                            )
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneLitre,
                        ProductValueFixture.water
                    )
                ),
                biosphere = emptyList(),
            ),
            ProcessValue(
                name = "salad_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.salad.withConstraint(
                            FromProcessRefValue(
                                "salad_production",
                                emptyMap(),
                                ConstraintFlag.IS_DEFAULT,
                            )
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withConstraint(
                            FromProcessRefValue(
                                "carrot_production", mapOf(
                                    "q_water" to QuantityValueFixture.oneLitre,
                                ),
                                ConstraintFlag.IS_DEFAULT,
                            )
                        )
                    )
                ),
                biosphere = emptyList(),
            ),
        ).naturalSorted()
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
                name = "carrot_production",
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
        val recursiveEvaluator = Evaluator(symbolTable)

        // when/then
        try {
            recursiveEvaluator.eval(expression)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals(
                "more than one process produces 'carrot' : [carrot_production, carrot_production_bis]",
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
                name = "carrot_production",
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
                                "carrot_production",
                                emptyMap(),
                            ),
                        )
                    )
                ),
                biosphere = emptyList()
            )
        )
        val recursiveEvaluator = Evaluator(symbolTable)

        // when/then
        try {
            recursiveEvaluator.eval(expression).processes.toSet()
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals(
                "more than one process produces 'carrot' : [carrot_production, carrot_production_bis]",
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
                name = "salad_production",
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
                                "carrot_production",
                                mapOf("q_water" to QuantityFixture.twoLitres),
                            )
                        )
                    )
                ),
                biosphere = emptyList()
            )
        )
        val recursiveEvaluator = Evaluator(symbolTable)

        // when
        val actual = recursiveEvaluator.eval(expression).processes.naturalSorted()

        // then
        val expected = setOf(
            ProcessValue(
                name = "salad_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.salad.withConstraint(
                            FromProcessRefValue(
                                "salad_production",
                                emptyMap(),
                                ConstraintFlag.IS_DEFAULT
                            )
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withConstraint(
                            FromProcessRefValue(
                                "carrot_production", mapOf(
                                    "q_water" to QuantityValueFixture.twoLitres
                                )
                            )
                        )
                    )
                ),
                biosphere = emptyList(),
            ),
            ProcessValue(
                name = "carrot_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withConstraint(
                            FromProcessRefValue(
                                "carrot_production",
                                mapOf(
                                    "q_water" to QuantityValueFixture.twoLitres
                                ),
                            ),
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.twoLitres,
                        ProductValueFixture.water
                    )
                ),
                biosphere = emptyList(),
            ),
        ).naturalSorted()
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
                name = "carrot_production",
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
                                "carrot_production",
                                mapOf("q_water" to QuantityFixture.twoLitres),
                            )
                        )
                    )
                ),
                biosphere = emptyList()
            )
        )
        val recursiveEvaluator = Evaluator(symbolTable)

        // when/then
        try {
            recursiveEvaluator.eval(expression)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals("no process 'carrot_production' providing 'irrelevant_product' found", e.message)
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
                name = "carrot_production",
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
        val recursiveEvaluator = Evaluator(symbolTable)

        // when
        val actual = recursiveEvaluator.eval(expression).substanceCharacterizations.toSet()

        // then
        val expected = setOf(
            SubstanceCharacterizationValueFixture.propanolCharacterization
        )
        assertEquals(expected, actual)
    }
}
