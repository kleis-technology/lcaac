package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.EBioExchange
import ch.kleis.lcaac.core.lang.expression.EImpact
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaac.core.lang.expression.ESubstanceSpec
import ch.kleis.lcaac.core.math.basic.BasicNumber

class SubstanceCharacterizationFixture {
    companion object {
        val propanolCharacterization = ESubstanceCharacterization(
            referenceExchange = EBioExchange(QuantityFixture.oneKilogram, SubstanceFixture.propanol),
            impacts = listOf(
                EImpact(QuantityFixture.oneKilogram, IndicatorFixture.climateChange),
            ),
        )

        fun substanceCharacterizationFor(substance: ESubstanceSpec<BasicNumber>): ESubstanceCharacterization<BasicNumber> =
            ESubstanceCharacterization(
                referenceExchange = EBioExchange(QuantityFixture.oneKilogram, substance),
                impacts = listOf(
                    EImpact(QuantityFixture.oneKilogram, IndicatorFixture.climateChange)
                )
            )
    }
}
