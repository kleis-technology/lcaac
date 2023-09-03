package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.math.basic.BasicNumber


class UnitValueFixture {
    companion object {
        fun <Q> ton() = UnitValue<Q>(UnitSymbol.of("ton"), 1000.0, DimensionFixture.mass)
        fun <Q> g() = UnitValue<Q>(UnitSymbol.of("g"), 1.0e-3, DimensionFixture.mass)
        fun <Q> kg() = UnitValue<Q>(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass)
        fun <Q> l() = UnitValue<Q>(UnitSymbol.of("l"), 1.0e-3, DimensionFixture.volume)
        fun <Q> percent() = UnitValue<Q>(UnitSymbol.of("percent"), 1.0e-2, Dimension.None)
        fun <Q> piece() = UnitValue<Q>(UnitSymbol.of("piece"), 1.0, Dimension.None)
        fun <Q> unit() = UnitValue<Q>(UnitSymbol.of("unit"), 1.0, Dimension.None)
    }
}
