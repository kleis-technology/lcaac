package ch.kleis.lcaplugin.core.prelude

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.EQuantityLiteral
import ch.kleis.lcaplugin.core.lang.expression.EUnitLiteral
import ch.kleis.lcaplugin.core.lang.expression.QuantityExpression
import ch.kleis.lcaplugin.core.lang.expression.UnitExpression


class Prelude {
    companion object {
        val mass = Dimension.of("mass")
        val length = Dimension.of("length")
        val area = length.multiply(length)
        val volume = length.multiply(area)
        val energy = Dimension.of("energy")
        val time = Dimension.of("time")
        val land_use = area.multiply(time)
        val power = energy.divide(time)
        val none = Dimension.None
        val radioactivity = Dimension.of("radioactivity")
        val unitMap = listOf(
            EUnitLiteral("u", 1.0, none),
            EUnitLiteral("piece", 1.0, none),
            EUnitLiteral("person", 1.0, none),
            EUnitLiteral("percent", 1.0e-2, none),
            EUnitLiteral("kg", 1.0, mass),
            EUnitLiteral("g", 1.0e-3, mass),
            EUnitLiteral("mm", 1.0e-3, length),
            EUnitLiteral("cm", 1.0e-2, length),
            EUnitLiteral("m", 1.0, length),
            EUnitLiteral("km", 1.0e3, length),
            EUnitLiteral("m2", 1.0, area),
            EUnitLiteral("ha", 1.0e4, area),
            EUnitLiteral("km2", 1.0e6, area),
            EUnitLiteral("m3", 1.0, volume),
            EUnitLiteral("l", 1.0e-3, volume),
            EUnitLiteral("cl", 1.0e-5, volume),
            EUnitLiteral("ml", 1.0e-6, volume),
            EUnitLiteral("Bq", 1.0, radioactivity),
            EUnitLiteral("kBq", 1000.0, radioactivity),
            EUnitLiteral("s", 1.0, time),
            EUnitLiteral("min", 60.0, time),
            EUnitLiteral("hour", 3600.0, time),
            EUnitLiteral("day", 24 * 3600.0, time),
            EUnitLiteral("year", 365 * 24 * 3600.0, time),
            EUnitLiteral("Wh", 1.0, energy),
            EUnitLiteral("kWh", 1.0e3, energy),
            EUnitLiteral("MWh", 1.0e6, energy),
            EUnitLiteral("J", 1.0 / 3600.0, energy),
            EUnitLiteral("kJ", 1.0e3 / 3600.0, energy),
            EUnitLiteral("MJ", 1.0e6 / 3600.0, energy),
            EUnitLiteral("W", 1.0, power),
            EUnitLiteral("m2a", 1.0, land_use),
        ).associateBy { it.symbol }

        val units: Register<UnitExpression> = Register(unitMap)
        val unitQuantities = Register<QuantityExpression>(
            unitMap.mapValues { EQuantityLiteral(1.0, it.value) }
        )
    }
}
