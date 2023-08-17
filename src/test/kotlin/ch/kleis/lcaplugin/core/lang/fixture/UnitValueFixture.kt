package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.value.UnitValue

class UnitValueFixture {
    companion object {
        val ton = UnitValue(UnitSymbol.of("ton"), 1000.0, DimensionFixture.mass)
        val m = UnitValue(UnitSymbol.of("m"), 1.0, DimensionFixture.length)
        val g = UnitValue(UnitSymbol.of("g"), 1.0e-3, DimensionFixture.mass)
        val kg = UnitValue(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass)
        val l = UnitValue(UnitSymbol.of("l"), 1.0e-3, DimensionFixture.volume)
        val percent = UnitValue(UnitSymbol.of("percent"), 1.0e-2, Dimension.None)
        val piece = UnitValue(UnitSymbol.of("piece"), 1.0, Dimension.None)
        val unit = UnitValue(UnitSymbol.of("unit"), 1.0, Dimension.None)
    }
}
