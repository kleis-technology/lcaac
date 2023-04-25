package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.value.BioExchangeValue

class BioExchangeValueFixture {
    companion object {
        val propanolBioExchange = BioExchangeValue(
            QuantityValueFixture.oneKilogram,
            SubstanceValueFixture.propanol
        )
    }
}