package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.value.BioExchangeValue
import ch.kleis.lcaac.core.lang.value.ImpactValue
import ch.kleis.lcaac.core.lang.value.SubstanceCharacterizationValue

class SubstanceCharacterizationValueFixture {
    companion object {
        val propanolCharacterization = SubstanceCharacterizationValue(
            referenceExchange = BioExchangeValue(
                QuantityValueFixture.oneKilogram,
                FullyQualifiedSubstanceValueFixture.propanol
            ),
            impacts = listOf(
                ImpactValue(QuantityValueFixture.oneKilogram, IndicatorValueFixture.climateChange),
            ),
        )
    }
}
