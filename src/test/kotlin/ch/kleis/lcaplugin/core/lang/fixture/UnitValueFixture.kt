package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.value.UnitValue

class UnitValueFixture {
    companion object {
        val ton = UnitValue("ton", 1000.0, DimensionFixture.mass)
        val m = UnitValue("m", 1.0, DimensionFixture.length)
        val kg = UnitValue("kg", 1.0, DimensionFixture.mass)
        val l = UnitValue("l", 1.0e-3, DimensionFixture.volume)
        val percent = UnitValue("percent", 1.0e-2, Dimension.None)
        val piece = UnitValue("piece", 1.0, Dimension.None)
    }
}
