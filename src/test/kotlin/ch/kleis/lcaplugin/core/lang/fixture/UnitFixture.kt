package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.UnitSymbol
import ch.kleis.lcaplugin.core.lang.expression.EUnitLiteral

class DimensionFixture {
    companion object {
        val mass = Dimension.of("mass")
        val length = Dimension.of("length")
        val time = Dimension.of("time")
        val volume = length.pow(3.0)
    }
}

class UnitFixture {
    companion object {
        val kg = EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass)
        val g = EUnitLiteral(UnitSymbol.of("g"), 1.0e-3, DimensionFixture.mass)
        val m = EUnitLiteral(UnitSymbol.of("m"), 1.0, DimensionFixture.length)
        val km = EUnitLiteral(UnitSymbol.of("km"), 1000.0, DimensionFixture.length)
        val person = EUnitLiteral(UnitSymbol.of("person"), 1.0, Dimension.None)
        val pack = EUnitLiteral(UnitSymbol.of("pack"), 1.0, Dimension.None)
        val l = EUnitLiteral(UnitSymbol.of("l"), 1.0e-3, DimensionFixture.volume)
        val s = EUnitLiteral(UnitSymbol.of("s"), 1.0, DimensionFixture.time)
        val hour = EUnitLiteral(UnitSymbol.of("hour"), 3600.0, DimensionFixture.time)
        val percent = EUnitLiteral(UnitSymbol.of("percent"), 1.0e-2, Dimension.None)
    }
}
