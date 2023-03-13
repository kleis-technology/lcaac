package ch.kleis.lcaplugin.core.prelude

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.EUnitLiteral


class Prelude {
    companion object {
        private val mass = Dimension.of("mass")
        private val length = Dimension.of("length")
        private val area = length.multiply(length)
        private val volume = length.multiply(area)
        private val energy = Dimension.of("energy")
        private val time = Dimension.of("time")
        private val power = energy.divide(time)
        private val none = Dimension.None
        private val radioactivity = Dimension.of("radioactivity")

        private val definitions = listOf(
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
            EUnitLiteral("Wh", 1.0, energy),
            EUnitLiteral("kWh", 1.0e3, energy),
            EUnitLiteral("MWh", 1.0e6, energy),
            EUnitLiteral("J", 1.0 / 3600.0, energy),
            EUnitLiteral("kJ", 1.0e3 / 3600.0, energy),
            EUnitLiteral("MJ", 1.0e6 / 3600.0, energy),
            EUnitLiteral("W", 1.0, power),
        ).associateBy { it.symbol }

        val packages = mapOf(
            Pair(
                "prelude.units",
                Package(
                    "prelude.units",
                    emptyList(),
                    SymbolTable(
                        units = Register(definitions)
                    )
                )
            )
        )
    }
}
