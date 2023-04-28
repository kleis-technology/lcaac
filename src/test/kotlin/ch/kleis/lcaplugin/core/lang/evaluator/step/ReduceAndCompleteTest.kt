package ch.kleis.lcaplugin.core.lang.evaluator.step

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.toValue
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.*
import ch.kleis.lcaplugin.core.lang.value.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test


class ReduceAndCompleteTest {

    @Test
    fun eval_whenInstanceOfProcessTemplate_shouldEvaluateToProcessValue() {
        // given
        val template = TemplateFixture.carrotProduction
        val instance = EProcessTemplateApplication(
            template, mapOf(
                Pair(
                    "q_water", EQuantityAdd(
                        QuantityFixture.oneLitre,
                        QuantityFixture.oneLitre,
                    )
                )
            )
        )
        val reduceAndComplete = ReduceAndComplete(SymbolTable.empty())

        // when
        val actual = reduceAndComplete.apply(instance).toValue()

        // then
        val expected = ProcessValue(
            "carrot_production",
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot.withFromProcessRef(
                        FromProcessRefValue(
                            "carrot_production",
                            mapOf("q_water" to QuantityValueFixture.twoLitres),
                        )
                    ),
                )
            ),
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoLitres,
                    ProductValueFixture.water,
                )
            ),
            emptyList(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_withUnknownSubstances_shouldCompleteSubstances() {
        // given
        val template = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            body = EProcess(
                name = "process",
                products = emptyList(),
                inputs = emptyList(),
                biosphere = listOf(
                    EBioExchange(QuantityFixture.oneKilogram, ESubstanceSpec("co2"))
                ),
            )
        )
        val instance = EProcessTemplateApplication(
            template, emptyMap()
        )
        val reduceAndComplete = ReduceAndComplete(SymbolTable.empty())

        // when
        val actual = reduceAndComplete.apply(instance).toValue()

        // then
        val expected = ProcessValue(
            name = "process",
            emptyList(),
            emptyList(),
            listOf(
                BioExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    SubstanceValue(
                        "co2",
                        type = SubstanceType.UNDEFINED,
                        "__unknown__",
                        null,
                        UnitValueFixture.kg,
                    )
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenProcessTemplate_shouldAutomaticallyInstantiateWithoutArguments() {
        // given
        val template = TemplateFixture.carrotProduction
        val reduceAndComplete = ReduceAndComplete(SymbolTable.empty())

        // when
        val actual = reduceAndComplete.apply(template).toValue()

        // then
        val expected = ProcessValue(
            name = "carrot_production",
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot.withFromProcessRef(
                        FromProcessRefValue(
                            "carrot_production",
                            mapOf("q_water" to QuantityValueFixture.oneLitre),
                        )
                    )
                )
            ),
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneLitre,
                    ProductValueFixture.water
                )
            ),
            emptyList(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenTemplateRef_shouldReadEnv() {
        // given
        val templateRef = EProcessTemplateRef("p")
        val reduceAndComplete = ReduceAndComplete(
            SymbolTable(
                processTemplates = Register.from(
                    hashMapOf(
                        Pair("p", TemplateFixture.carrotProduction)
                    )
                )
            )
        )

        // when
        val actual = reduceAndComplete.apply(templateRef).toValue()

        // then
        val expected = ProcessValue(
            name = "carrot_production",
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot.withFromProcessRef(
                        FromProcessRefValue(
                            "carrot_production",
                            mapOf("q_water" to QuantityValueFixture.oneLitre),
                        )
                    )
                )
            ),
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneLitre,
                    ProductValueFixture.water
                )
            ),
            emptyList(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenContainsUnboundedReference_shouldThrow() {
        // given
        val template = TemplateFixture.withUnboundedRef
        val reduceAndComplete = ReduceAndComplete(SymbolTable.empty())

        // when/then
        try {
            reduceAndComplete.apply(template)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals("unbounded references: [q_carrot, q_water]", e.message)
        }
    }

    @Test
    fun eval_whenSubstanceCharacterization_shouldCompleteIndicator() {
        // given
        val expression = ESubstanceCharacterization(
            EBioExchange(QuantityFixture.oneKilogram, SubstanceFixture.propanol),
            listOf(
                EImpact(QuantityFixture.oneKilogram, EIndicatorRef("cc"))
            )
        )
        val reduceAndComplete = ReduceAndComplete(SymbolTable.empty())

        // when
        val actual = reduceAndComplete.apply(expression).toValue()

        // then
        val expected = SubstanceCharacterizationValue(
            BioExchangeValue(QuantityValueFixture.oneKilogram, SubstanceValueFixture.propanol),
            listOf(
                ImpactValue(
                    QuantityValueFixture.oneKilogram,
                    IndicatorValue("cc", UnitValueFixture.kg)
                )
            )
        )
        assertEquals(expected, actual)
    }
}
