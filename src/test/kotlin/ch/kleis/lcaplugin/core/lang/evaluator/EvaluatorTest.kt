package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.EInstance
import ch.kleis.lcaplugin.core.lang.expression.EQuantityAdd
import ch.kleis.lcaplugin.core.lang.expression.ETemplateRef
import ch.kleis.lcaplugin.core.lang.fixture.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test


class EvaluatorTest {

    @Test
    fun eval_whenInstanceOfProcessTemplate_shouldEvaluateToProcessValue() {
        // given
        val template = TemplateFixture.carrotProduction
        val instance = EInstance(
            template, mapOf(
                Pair(
                    "q_water", EQuantityAdd(
                        QuantityFixture.oneLitre,
                        QuantityFixture.oneLitre,
                    )
                )
            )
        )
        val evaluator = Evaluator()

        // when
        val actual = evaluator.eval(instance)

        // then
        val expected = ProcessValue(
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot,
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
    fun eval_whenProcessTemplate_shouldAutomaticallyInstantiateWithoutArguments() {
        // given
        val template = TemplateFixture.carrotProduction
        val evaluator = Evaluator()

        // when
        val actual = evaluator.eval(template)

        // then
        val expected = ProcessValue(
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot
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
        val templateRef = ETemplateRef("p")
        val evaluator = Evaluator(
            SymbolTable(
                processTemplates = Register(
                    hashMapOf(
                        Pair("p", TemplateFixture.carrotProduction)
                    )
                )
            )
        )

        // when
        val actual = evaluator.eval(templateRef)

        // then
        val expected = ProcessValue(
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot
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
        val evaluator = Evaluator()

        // when/then
        try {
            evaluator.eval(template)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals("unbounded references: [q_carrot, q_water]", e.message)
        }
    }
}
