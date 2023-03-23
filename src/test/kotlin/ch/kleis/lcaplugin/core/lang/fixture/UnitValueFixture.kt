package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.UnitValue

class UnitValueFixture {
    companion object {
        val m = UnitValue("m", 1.0, DimensionFixture.length)
        val kg = UnitValue("kg", 1.0, DimensionFixture.mass)
        val l = UnitValue("l", 1.0e-3, DimensionFixture.volume)
    }
}
