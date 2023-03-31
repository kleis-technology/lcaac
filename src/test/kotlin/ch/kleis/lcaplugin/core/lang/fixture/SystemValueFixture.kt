package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.fixture.ProcessValueFixture.Companion.carrotProcessValue
import ch.kleis.lcaplugin.core.lang.fixture.ProcessValueFixture.Companion.carrotProcessValueWithAllocation
import ch.kleis.lcaplugin.core.lang.value.SystemValue

class SystemValueFixture {
    companion object {
        val carrotSystem = SystemValue(setOf(carrotProcessValue), setOf())
        val carrotSystemWithAllocation = SystemValue(setOf(carrotProcessValueWithAllocation), setOf())
    }
}