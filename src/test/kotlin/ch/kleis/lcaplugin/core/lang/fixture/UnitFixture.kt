package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.Dimension
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
        val kg = EUnitLiteral("kg", 1.0, DimensionFixture.mass)
        val g = EUnitLiteral("g", 1.0e-3, DimensionFixture.mass)
        val m = EUnitLiteral("m", 1.0, DimensionFixture.length)
        val km = EUnitLiteral("km", 1000.0, DimensionFixture.length)
        val person = EUnitLiteral("person", 1.0, Dimension.None)
        val pack = EUnitLiteral("pack", 1.0, Dimension.None)
        val l = EUnitLiteral("l", 1.0e-3, DimensionFixture.volume)
        val s = EUnitLiteral("s", 1.0, DimensionFixture.time)
        val hour = EUnitLiteral("hour", 3600.0, DimensionFixture.time)
    }
}
