package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.math.basic.BasicNumber


class UnitValueFixture {
    companion object {
        val ton = UnitValue<BasicNumber>(UnitSymbol.of("ton"), 1000.0, DimensionFixture.mass)
        val m = UnitValue<BasicNumber>(UnitSymbol.of("m"), 1.0, DimensionFixture.length)
        val g = UnitValue<BasicNumber>(UnitSymbol.of("g"), 1.0e-3, DimensionFixture.mass)
        val kg = UnitValue<BasicNumber>(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass)
        val l = UnitValue<BasicNumber>(UnitSymbol.of("l"), 1.0e-3, DimensionFixture.volume)
        val percent = UnitValue<BasicNumber>(UnitSymbol.of("percent"), 1.0e-2, Dimension.None)
        val piece = UnitValue<BasicNumber>(UnitSymbol.of("piece"), 1.0, Dimension.None)
        val unit = UnitValue<BasicNumber>(UnitSymbol.of("unit"), 1.0, Dimension.None)
    }
}
