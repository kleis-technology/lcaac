package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.fixture.IndicatorValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceValueFixture
import ch.kleis.lcaplugin.core.lang.value.BioExchangeValue
import ch.kleis.lcaplugin.core.lang.value.ImpactValue
import ch.kleis.lcaplugin.core.lang.value.TechnoExchangeValue
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
            assertEquals("incompatible dimensions: mass[1.0] vs length[3.0]", e.message)
        }
    }

    @Test
    fun bioExchange_whenDimensionsDontMatch_thenThrows() {
        // given
        val quantity = QuantityValueFixture.oneLitre
        val substance = SubstanceValueFixture.propanol

        // when/then
        try {
            BioExchangeValue(quantity, substance)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals("incompatible dimensions: length[3.0] vs mass[1.0]", e.message)
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
            assertEquals("incompatible dimensions: length[3.0] vs mass[1.0]", e.message)
        }
    }
}
