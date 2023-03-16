package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EBioExchange
import ch.kleis.lcaplugin.core.lang.expression.EImpact
import ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization

class SubstanceCharacterizationFixture {
    companion object {
        val propanolCharacterization = ESubstanceCharacterization(
            referenceExchange = EBioExchange(QuantityFixture.oneKilogram, SubstanceFixture.propanol),
            impacts = listOf(
                EImpact(QuantityFixture.oneKilogram, IndicatorFixture.climateChange),
            ),
        )
    }
}
