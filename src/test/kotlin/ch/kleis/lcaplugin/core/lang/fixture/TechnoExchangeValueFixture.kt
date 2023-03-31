package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.value.TechnoExchangeValue

class TechnoExchangeValueFixture {
    companion object {
        val carrotTechnoExchangeValue = TechnoExchangeValue(
            QuantityValueFixture.oneKilogram,
            ProductValueFixture.carrot
        )

        val carrotTechnoExchangeValueWithAllocation = TechnoExchangeValue(
            QuantityValueFixture.oneKilogram,
            ProductValueFixture.carrot,
            QuantityValueFixture.fiftyPercent
        )

        val waterTechnoExchangeValueWithAllocation = TechnoExchangeValue(
            QuantityValueFixture.oneLitre,
            ProductValueFixture.water
        )
    }
}