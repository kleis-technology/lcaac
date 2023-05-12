package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.fixture.FullyQualifiedSubstanceValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.IndicatorValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class ExchangeValueTest {
    @Test
    fun technoExchange_whenDimensionsDontMatch_thenThrows() {
        // given
        val quantity = QuantityValueFixture.oneKilogram
        val product = ProductValueFixture.water

        // when/then
        try {
            TechnoExchangeValue(quantity, product)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals("incompatible dimensions: mass vs length³ for product water", e.message)
        }
    }

    @Test
    fun bioExchange_whenDimensionsDontMatch_thenThrows() {
        // given
        val quantity = QuantityValueFixture.oneLitre
        val substance = FullyQualifiedSubstanceValueFixture.propanol

        // when/then
        try {
            BioExchangeValue(quantity, substance)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals(
                "incompatible dimensions: length³ vs mass for substance [Resource] propanol(air), quantity=1.0",
                e.message
            )
        }
    }

    @Test
    fun impact_whenDimensionsDontMatch_thenThrows() {
        // given
        val quantity = QuantityValueFixture.oneLitre
        val indicator = IndicatorValueFixture.climateChange

        // when/then
        try {
            ImpactValue(quantity, indicator)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals(
                "incompatible dimensions: length³ vs mass for indicator climate change, quantity=1.0",
                e.message
            )
        }
    }
}
