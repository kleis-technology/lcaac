package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.value.UnitValue


class UnitValueFixture {
    companion object {
        fun <Q> ton() = UnitValue<Q>(UnitSymbol.of("ton"), 1000.0, DimensionFixture.mass)
        fun <Q> g() = UnitValue<Q>(UnitSymbol.of("g"), 1.0e-3, DimensionFixture.mass)
        fun <Q> kg() = UnitValue<Q>(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass)
        fun <Q> l() = UnitValue<Q>(UnitSymbol.of("l"), 1.0e-3, DimensionFixture.volume)
        fun <Q> percent() = UnitValue<Q>(UnitSymbol.of("percent"), 1.0e-2, Dimension.None)
        fun <Q> piece() = UnitValue<Q>(UnitSymbol.of("piece"), 1.0, Dimension.None)
        fun <Q> unit() = UnitValue<Q>(UnitSymbol.of("u"), 1.0, Dimension.None)
        fun <Q> gb() = UnitValue<Q>(UnitSymbol.of("GB"), 1.0, DimensionFixture.memory)
        fun <Q> tb() = UnitValue<Q>(UnitSymbol.of("TB"), 1.0e3, DimensionFixture.memory)
        fun <Q> hour() = UnitValue<Q>(UnitSymbol.of("hour"), 3600.0, DimensionFixture.time)
        fun <Q> watt() = UnitValue<Q>(UnitSymbol.of("W"), 1.0, DimensionFixture.power)
    }
}
