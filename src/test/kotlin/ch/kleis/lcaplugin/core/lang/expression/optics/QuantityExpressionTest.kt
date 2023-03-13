package ch.kleis.lcaplugin.core.lang.expression.optics

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class QuantityExpressionTest {
    @Test
    fun optics_quantityReferencesInQuantity_getAll() {
        // given
        val expression = EQuantityAdd(
            EQuantityRef("x"),
            EQuantityDiv(
                EQuantityRef("y"),
                EQuantityRef("z"),
            )
        )

        // when
        val actual = everyQuantityRefInQuantityExpression.getAll(expression)

        // then
        val expected = listOf(EQuantityRef("x"), EQuantityRef("y"), EQuantityRef("z"))
        assertEquals(expected, actual)
    }

    @Test
    fun optics_quantityReferencesInQuantity_shouldHandleComplexExpressions() {
        // given
        val expression = EQuantityAdd(
            EQuantityRef("x"),
            EQuantityDiv(
                EQuantityRef("y"),
                EQuantityRef("x"),
            )
        )
        val map: (EQuantityRef) -> QuantityExpression = { ref ->
            if (ref.name == "x") QuantityFixture.oneKilogram else ref
        }

        // when
        val actual = everyQuantityRefInQuantityExpression.modify(expression, map)

        // then
        val expected = EQuantityAdd(
            QuantityFixture.oneKilogram,
            EQuantityDiv(
                EQuantityRef("y"),
                QuantityFixture.oneKilogram,
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun optics_unboundedQuantityRefInTemplate_getAll() {
        // given
        val template = EProcessTemplate(
            mapOf("a" to QuantityFixture.oneKilogram),
            mapOf("b" to QuantityFixture.oneLitre),
            EProcess(
                listOf(ETechnoExchange(EQuantityRef("a"), ProductFixture.carrot)),
                listOf(
                    ETechnoExchange(
                        EQuantityAdd(
                            EQuantityRef("b"),
                            EQuantityRef("x"),
                        ),
                        ProductFixture.water
                    )
                ),
                listOf(EBioExchange(EQuantityRef("y"), SubstanceFixture.propanol)),
            )
        )

        // when
        val actual = everyUnboundedQuantityRefInTemplateExpression.getAll(template).map { it.name }

        // then
        val expected = listOf("x", "y")
        assertEquals(expected, actual)
    }

    @Test
    fun optics_unboundedQuantityRefInTemplate_modify() {
        // given
        val template = EProcessTemplate(
            mapOf("a" to QuantityFixture.oneKilogram),
            mapOf("b" to QuantityFixture.oneLitre),
            EProcess(
                listOf(ETechnoExchange(EQuantityRef("a"), ProductFixture.carrot)),
                listOf(
                    ETechnoExchange(
                        EQuantityAdd(
                            EQuantityRef("b"),
                            EQuantityRef("x"),
                        ),
                        ProductFixture.water
                    )
                ),
                listOf(EBioExchange(EQuantityRef("y"), SubstanceFixture.propanol)),
            )
        )

        // when
        val actual = everyUnboundedQuantityRefInTemplateExpression.modify(template) {
            if (it.name == "x") QuantityFixture.oneKilogram else it
        }

        // then
        val expected = EProcessTemplate(
            mapOf("a" to QuantityFixture.oneKilogram),
            mapOf("b" to QuantityFixture.oneLitre),
            EProcess(
                listOf(ETechnoExchange(EQuantityRef("a"), ProductFixture.carrot)),
                listOf(
                    ETechnoExchange(
                        EQuantityAdd(
                            EQuantityRef("b"),
                            QuantityFixture.oneKilogram,
                        ),
                        ProductFixture.water
                    )
                ),
                listOf(EBioExchange(EQuantityRef("y"), SubstanceFixture.propanol)),
            )
        )
        assertEquals(expected, actual)
    }
}
