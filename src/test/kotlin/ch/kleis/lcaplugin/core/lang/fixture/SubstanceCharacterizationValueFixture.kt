package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.BioExchangeValue
import ch.kleis.lcaplugin.core.lang.ImpactValue
import ch.kleis.lcaplugin.core.lang.SubstanceCharacterizationValue

class SubstanceCharacterizationValueFixture {
    companion object {
        val propanolCharacterization = SubstanceCharacterizationValue(
            referenceExchange = BioExchangeValue(QuantityValueFixture.oneKilogram, SubstanceValueFixture.propanol),
            impacts = listOf(
                ImpactValue(QuantityValueFixture.oneKilogram, IndicatorValueFixture.climateChange),
            ),
        )
    }
}
