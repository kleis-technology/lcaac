package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.fixture.ProcessValueFixture.Companion.carrotProcessValue
import ch.kleis.lcaplugin.core.lang.fixture.ProcessValueFixture.Companion.carrotProcessValueWithAllocation
import ch.kleis.lcaplugin.core.lang.value.SystemValue

class SystemValueFixture {
    companion object {
        fun carrotSystem() = SystemValue(mutableSetOf(carrotProcessValue), mutableSetOf())
        fun carrotSystemWithAllocation() = SystemValue(mutableSetOf(carrotProcessValueWithAllocation), mutableSetOf())
    }
}