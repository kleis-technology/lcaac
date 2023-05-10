package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.fixture.TechnoExchangeValueFixture.Companion.carrotTechnoExchangeValue
import ch.kleis.lcaplugin.core.lang.fixture.TechnoExchangeValueFixture.Companion.carrotTechnoExchangeValueWithAllocation
import ch.kleis.lcaplugin.core.lang.fixture.TechnoExchangeValueFixture.Companion.waterTechnoExchangeValueWithAllocation
import ch.kleis.lcaplugin.core.lang.value.ProcessValue

class ProcessValueFixture {
    companion object {
        val carrotProcessValue = ProcessValue(
            "carrot",
            listOf(carrotTechnoExchangeValue),
            listOf(),
            listOf()
        )

        val carrotProcessValueWithAllocation = ProcessValue(
            "carrot",
            listOf(carrotTechnoExchangeValueWithAllocation),
            listOf(waterTechnoExchangeValueWithAllocation),
            listOf()
        )
    }
}
