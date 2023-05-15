package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.fixture.FullyQualifiedSubstanceValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.IndicatorValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import org.junit.Test
import kotlin.test.assertFailsWith

class ExchangeValueTest {
    @Test
    fun technoExchange_whenDimensionsDoNotMatch_thenThrows() {
        // given
        val quantity = QuantityValueFixture.oneKilogram
        val product = ProductValueFixture.water

        // when/then
        assertFailsWith(
                EvaluatorException::class,
                "incompatible dimensions: mass vs length³ for product water"
        ) { TechnoExchangeValue(quantity, product) }
    }

    @Test
    fun bioExchange_whenDimensionsDoNotMatch_thenThrows() {
        // given
        val quantity = QuantityValueFixture.oneLitre
        val substance = FullyQualifiedSubstanceValueFixture.propanol

        // when
        val sut: () -> Unit = { BioExchangeValue(quantity, substance) }
        assertFailsWith(
                EvaluatorException::class,
                "incompatible dimensions: length³ vs mass for substance [Resource] propanol(air), quantity=1.0",
                sut
        )
    }

    @Test
    fun impact_whenDimensionsDoNotMatch_thenThrows() {
        // given
        val quantity = QuantityValueFixture.oneLitre
        val indicator = IndicatorValueFixture.climateChange

        // When
        val sut: () -> Unit = { ImpactValue(quantity, indicator) }

        // when/then
        assertFailsWith(
                EvaluatorException::class,
                "incompatible dimensions: length³ vs mass for indicator climate change, quantity=1.0",
                sut
        )
    }
}
