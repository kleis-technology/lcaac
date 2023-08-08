package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.fixture.TechnoExchangeValueFixture.Companion.carrotTechnoExchangeValue
import ch.kleis.lcaplugin.core.lang.fixture.TechnoExchangeValueFixture.Companion.carrotTechnoExchangeValueWithAllocation
import ch.kleis.lcaplugin.core.lang.fixture.TechnoExchangeValueFixture.Companion.waterTechnoExchangeValueWithAllocation
import ch.kleis.lcaplugin.core.lang.value.ProcessValue

class ProcessValueFixture {
    companion object {
        val carrotProcessValue = ProcessValue(
            "carrot",
            labels = emptyMap(),
            products = listOf(carrotTechnoExchangeValue),
            inputs = emptyList(),
            biosphere = emptyList(),
            impacts = emptyList(),
        )

        val carrotProcessValueWithAllocation = ProcessValue(
            "carrot",
            labels = emptyMap(),
            products = listOf(carrotTechnoExchangeValueWithAllocation),
            inputs = listOf(waterTechnoExchangeValueWithAllocation),
            biosphere = emptyList(),
            impacts = emptyList(),
        )
    }
}
