package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.*
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.*
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class EvaluatorTest {
    private val ops = BasicOperations

    @Test
    fun eval_processWithImpacts_shouldReduceImpacts() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
                name = "eProcess",
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot)
                ),
                impacts = listOf(
                    ImpactFixture.oneClimateChange
                ),
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = ProcessTemplateRegister(
                mapOf(
                    ProcessKey("eProcess") to template,
                )
            )
        )
        val evaluator = Evaluator(symbolTable, ops)
        val expected = ImpactValue(
            QuantityValueFixture.oneKilogram,
            IndicatorValueFixture.climateChange,
        )

        // when
        val actual = evaluator.trace(template).getEntryPoint().impacts.first()

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun eval_unresolvedSubstance_shouldBeTreatedAsTerminal() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
                "eProcess",
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, EProductSpec("p", QuantityFixture.oneKilogram))
                ),
                biosphere = listOf(
                    EBioExchange(
                        QuantityFixture.oneKilogram,
                        ESubstanceSpec(
                            "doesNotExist",
                            "doesNotExist",
                            SubstanceType.EMISSION,
                            "water",
                            "sea water"
                        )
                    )
                ),
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = ProcessTemplateRegister.from(
                mapOf(
                    ProcessKey("eProcess") to template
                )
            )
        )
        val evaluator = Evaluator(symbolTable, ops)
        val expected = FullyQualifiedSubstanceValue<BasicNumber>(
            "doesNotExist",
            type = SubstanceType.EMISSION,
            compartment = "water",
            subcompartment = "sea water",
            referenceUnit = UnitValue(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass)
        )

        // when
        val actual = evaluator.trace(template).getEntryPoint().biosphere.first().substance

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenTwoInstancesOfSameTemplate_thenDifferentProduct() {
        // given
        val template = TemplateFixture.carrotProduction
        val symbolTable = SymbolTable(
            processTemplates = ProcessTemplateRegister.from(
                mapOf(
                    ProcessKey("carrot_production") to template,
                )
            )
        )
        val evaluator = Evaluator(symbolTable, ops)

        // when
        val p1 = evaluator.trace(template, mapOf("q_water" to QuantityFixture.oneLitre))
            .getEntryPoint().products.first().product
        val p2 = evaluator.trace(template, mapOf("q_water" to QuantityFixture.twoLitres))
            .getEntryPoint().products.first().product

        // then
        assertEquals(p1.name, p2.name)
        assertEquals(p1.referenceUnit, p2.referenceUnit)
        assertNotEquals(p1, p2)
    }

    @Test
    @Timeout(2)
    fun eval_whenAProductAsACycle_thenItShouldEnd() {
        // given
        val template = TemplateFixture.cyclicProduction
        val register = ProcessTemplateRegister(mapOf(ProcessKey("carrot_production") to template))

        val symbolTable = SymbolTable(processTemplates = register)
        val evaluator = Evaluator(symbolTable, BasicOperations)

        // when

        val p1 = evaluator.trace(template).getEntryPoint().products.first().product

        // then
        assertEquals("carrot", p1.name)
    }

    @Test
    fun eval_withImplicitProcessResolution_thenCorrectSystem() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
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
                        )
                    )
                ),
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = ProcessTemplateRegister(
                mapOf(
                    ProcessKey("carrot_production") to TemplateFixture.carrotProduction,
                    ProcessKey("salad_production") to template,
                )
            ),
        )
        val evaluator = Evaluator(symbolTable, ops)

        // when
        val actual = evaluator.trace(template).getSystemValue().processes

        // then
        val expected = setOf(
            ProcessValue(
                name = "carrot_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessRefValue(
                                name = "carrot_production",
                                arguments = mapOf(
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
            ),
            ProcessValue(
                name = "salad_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.salad.withFromProcessRef(
                            FromProcessRefValue(
                                name = "salad_production",
                            )
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessRefValue(
                                name = "carrot_production",
                                arguments = mapOf(
                                    "q_water" to QuantityValueFixture.oneLitre,
                                ),
                            )
                        )
                    )
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenExistsFromProcessRef_thenCorrectSystem() {
        val template = EProcessTemplate(
            body = EProcess(
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
                                MatchLabels(emptyMap()),
                                mapOf("q_water" to QuantityFixture.twoLitres),
                            ),
                        )
                    )
                ),
            ),
        )
        // given
        val processTemplates=  ProcessTemplateRegister(
            mapOf(
                ProcessKey("carrot_production") to TemplateFixture.carrotProduction,
                ProcessKey("salad_production") to template,
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val evaluator = Evaluator(symbolTable, ops)

        // when
        val actual = evaluator.trace(template).getSystemValue().processes

        // then
        val expected = setOf(
            ProcessValue(
                name = "salad_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.salad.withFromProcessRef(
                            FromProcessRefValue(
                                name = "salad_production",
                            )
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessRefValue(
                                name = "carrot_production",
                                arguments = mapOf(
                                    "q_water" to QuantityValueFixture.twoLitres
                                )
                            )
                        )
                    )
                ),
            ),
            ProcessValue(
                name = "carrot_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessRefValue(
                                "carrot_production",
                                arguments = mapOf(
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
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenProductDoesNotMatchProcess_shouldThrow() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
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
                                MatchLabels(emptyMap()),
                                mapOf("q_water" to QuantityFixture.twoLitres),
                            ),
                        )
                    )
                ),
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = ProcessTemplateRegister(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction,
                    "salad_production" to template,
                ).mapKeys { ProcessKey(it.key) }
            )
        )
        val evaluator = Evaluator(symbolTable, ops)

        // when/then
        val e = assertFailsWith(
            EvaluatorException::class,
        ) { evaluator.trace(template) }
        assertEquals("no process 'carrot_production' providing 'irrelevant_product' found", e.message)
    }

    @Test
    fun eval_whenNonEmptyBiosphere_thenIncludeSubstanceCharacterization() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.salad,
                    )
                ),
                biosphere = listOf(
                    EBioExchange(
                        QuantityFixture.oneKilogram, ESubstanceSpec(
                            "propanol",
                            compartment = "air",
                            type = SubstanceType.RESOURCE,
                        )
                    )
                ),
            )
        )
        val symbolTable = SymbolTable(
            substanceCharacterizations = SubstanceCharacterizationRegister(
                mapOf(
                    SubstanceKey("propanol", SubstanceType.RESOURCE, "air") to SubstanceCharacterizationFixture.propanolCharacterization,
                )
            ),
            processTemplates = ProcessTemplateRegister(
                mapOf(
                    ProcessKey("carrot_production") to template,
                )
            )
        )
        val evaluator = Evaluator(symbolTable, ops)

        // when
        val actual = evaluator.trace(template).getSystemValue().substanceCharacterizations

        // then
        val expected = setOf(
            SubstanceCharacterizationValueFixture.propanolCharacterization
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenProductUnitNotMatchProcess_shouldThrow() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
                name = "salad_production",
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.salad,
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneLitre,
                        ProductFixture.carrot,
                    )
                ),
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = ProcessTemplateRegister(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction,
                    "salad_production" to template,
                ).mapKeys { ProcessKey(it.key) }
            )
        )
        val evaluator = Evaluator(symbolTable, ops)

        // when/then
        val e = assertFailsWith(
            EvaluatorException::class,
        ) { evaluator.trace(template) }
        assertEquals("incompatible dimensions: length³ vs mass for product carrot", e.message)
    }
}
