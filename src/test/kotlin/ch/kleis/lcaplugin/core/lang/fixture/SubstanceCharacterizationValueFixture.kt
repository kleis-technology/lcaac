package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.value.BioExchangeValue
import ch.kleis.lcaplugin.core.lang.value.ImpactValue
import ch.kleis.lcaplugin.core.lang.value.SubstanceCharacterizationValue

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
