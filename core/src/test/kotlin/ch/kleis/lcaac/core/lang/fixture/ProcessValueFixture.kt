package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.fixture.TechnoExchangeValueFixture.Companion.carrotTechnoExchangeValue
import ch.kleis.lcaac.core.lang.fixture.TechnoExchangeValueFixture.Companion.carrotTechnoExchangeValueWithAllocation
import ch.kleis.lcaac.core.lang.fixture.TechnoExchangeValueFixture.Companion.waterTechnoExchangeValueWithAllocation
import ch.kleis.lcaac.core.lang.value.ProcessValue

class ProcessValueFixture {
    companion object {
        val carrotProcessValue = ProcessValue(
            name = "carrot",
            products = listOf(carrotTechnoExchangeValue),
        )

        val carrotProcessValueWithAllocation = ProcessValue(
            name = "carrot",
            products = listOf(carrotTechnoExchangeValueWithAllocation),
            inputs = listOf(waterTechnoExchangeValueWithAllocation),
        )
    }
}
