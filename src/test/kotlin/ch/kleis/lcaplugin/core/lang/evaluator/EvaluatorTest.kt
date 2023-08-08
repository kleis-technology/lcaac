package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.*
import ch.kleis.lcaplugin.core.lang.value.*
import com.intellij.openapi.ui.naturalSorted
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class EvaluatorTest {
    @Test
    fun eval_unresolvedSubstance_shouldBeTreatedAsTerminal() {
        // given
        val symbolTable = SymbolTable.empty()
        val instance = EProcessTemplateApplication(EProcessTemplate(
            params = mapOf(),
            locals = mapOf(),
            body = EProcess(
                "eProcess",
                products = emptyList(),
                labels = emptyMap(),
                inputs = emptyList(),
                biosphere = listOf(
                    EBioExchange(
                        EQuantityScale(1.0, EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass)),
                        ESubstanceSpec("doesNotExist",
                            "doesNotExist",
                            SubstanceType.EMISSION,
                            "water",
                            "sea water"
                        )
                    )
                ),
                impacts = emptyList(),
            )
        ), emptyMap())
        val evaluator = Evaluator(symbolTable)
        val expected = FullyQualifiedSubstanceValue("doesNotExist",
            type = SubstanceType.EMISSION,
            compartment = "water",
            subcompartment = "sea water",
            referenceUnit = UnitValue(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass)
        )

        // when
        val actual = evaluator.eval(instance).processes.first().biosphere.first().substance

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenTwoInstancesOfSameTemplate_thenDifferentProduct() {
        // given
        val template = TemplateFixture.carrotProduction
        val i1 = EProcessTemplateApplication(template, mapOf("q_water" to QuantityFixture.oneLitre))
        val i2 = EProcessTemplateApplication(template, mapOf("q_water" to QuantityFixture.twoLitres))
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
        val processTemplates: Register<EProcessTemplate> = Register.from(
            mapOf(
                "carrot_production" to TemplateFixture.carrotProduction
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val expression = EProcessTemplateApplication(EProcessTemplate(
            emptyMap(),
            emptyMap(),
            EProcess(
                name = "salad_production",
                labels = emptyMap(),
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.salad,
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EProductSpec(
                            "carrot",
                        )
                    )
                ),
                biosphere = emptyList(),
                impacts = emptyList(),
            )
        ), emptyMap())
        val recursiveEvaluator = Evaluator(symbolTable)

        // when
        val actual = recursiveEvaluator.eval(expression).processes.naturalSorted()

        // then
        val expected = setOf(
            ProcessValue(
                name = "carrot_production",
                labels = emptyMap(),
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessRefValue(
                                "carrot_production",
                                emptyMap(),
                                mapOf(
                                    "q_water" to QuantityValueFixture.oneLitre
                                ),
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
                impacts = emptyList(),
            ),
            ProcessValue(
                name = "salad_production",
                labels = emptyMap(),
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.salad.withFromProcessRef(
                            FromProcessRefValue(
                                "salad_production",
                                emptyMap(),
                                emptyMap(),
                            )
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessRefValue(
                                "carrot_production",
                                emptyMap(),
                                mapOf(
                                    "q_water" to QuantityValueFixture.oneLitre,
                                ),
                            )
                        )
                    )
                ),
                biosphere = emptyList(),
                impacts = emptyList(),
            ),
        ).naturalSorted()
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenExistsFromProcessRef_thenCorrectSystem() {
        // given
        val processTemplates: Register<EProcessTemplate> = Register.from(
            mapOf(
                "carrot_production" to TemplateFixture.carrotProduction
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val expression = EProcessTemplateApplication(EProcessTemplate(
            emptyMap(),
            emptyMap(),
            EProcess(
                name = "salad_production",
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.salad,
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EProductSpec(
                            "carrot",
                            UnitFixture.kg,
                            FromProcess(
                                "carrot_production",
                                MatchLabels.EMPTY,
                                mapOf("q_water" to QuantityFixture.twoLitres),
                            ),
                        )
                    )
                ),
                biosphere = emptyList(),
                impacts = emptyList(),
                labels = emptyMap(),
            )
        ), emptyMap())
        val recursiveEvaluator = Evaluator(symbolTable)

        // when
        val actual = recursiveEvaluator.eval(expression).processes.naturalSorted()

        // then
        val expected = setOf(
            ProcessValue(
                name = "salad_production",
                labels = emptyMap(),
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.salad.withFromProcessRef(
                            FromProcessRefValue(
                                "salad_production",
                                emptyMap(),
                                emptyMap(),
                            )
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessRefValue(
                                "carrot_production",
                                emptyMap(),
                                mapOf(
                                    "q_water" to QuantityValueFixture.twoLitres
                                )
                            )
                        )
                    )
                ),
                biosphere = emptyList(),
                impacts = emptyList(),
            ),
            ProcessValue(
                name = "carrot_production",
                labels = emptyMap(),
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessRefValue(
                                "carrot_production",
                                emptyMap(),
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
                impacts = emptyList(),
            ),
        ).naturalSorted()
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenProductDoesNotMatchProcess_shouldThrow() {
        // given
        val symbolTable = SymbolTable(
            processTemplates = Register.from(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction
                )
            )
        )
        val expression = EProcessTemplateApplication(EProcessTemplate(
            emptyMap(),
            emptyMap(),
            EProcess(
                name = "salad_production",
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.salad,
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EProductSpec(
                            "irrelevant_product",
                            UnitFixture.kg,
                            FromProcess(
                                "carrot_production",
                                MatchLabels.EMPTY,
                                mapOf("q_water" to QuantityFixture.twoLitres),
                            ),
                        )
                    )
                ),
                biosphere = emptyList(),
                impacts = emptyList(),
                labels = emptyMap(),
            )
        ), emptyMap())
        val recursiveEvaluator = Evaluator(symbolTable)

        // when/then
        val e = assertFailsWith(
            EvaluatorException::class,
        ) { recursiveEvaluator.eval(expression) }
        assertEquals("no process 'carrot_production' providing 'irrelevant_product' found", e.message)
    }

    @Test
    fun eval_whenNonEmptyBiosphere_thenIncludeSubstanceCharacterization() {
        // given
        val symbolTable = SymbolTable(
            substanceCharacterizations = Register.from(
                mapOf(
                    "propanol" to SubstanceCharacterizationFixture.propanolCharacterization,
                )
            ),
        )
        val expression = EProcessTemplateApplication(EProcessTemplate(
            emptyMap(),
            emptyMap(),
            EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.salad,
                    )
                ),
                inputs = emptyList(),
                biosphere = listOf(
                    EBioExchange(
                        QuantityFixture.oneKilogram, ESubstanceSpec(
                        "propanol",
                        compartment = "air",
                        type = SubstanceType.RESOURCE,
                    )
                    )
                ),
                impacts = emptyList(),
                labels = emptyMap(),
            )
        ), emptyMap())
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
