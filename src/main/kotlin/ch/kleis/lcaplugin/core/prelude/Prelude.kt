package ch.kleis.lcaplugin.core.prelude

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.EUnit
import ch.kleis.lcaplugin.core.lang.Environment
import ch.kleis.lcaplugin.core.lang.Package

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
            EUnit("one", 1.0, none),
            EUnit("percent", 1.0e-2, none),
            EUnit("kg", 1.0, mass),
            EUnit("g", 1.0e-3, mass),
            EUnit("mm", 1.0e-3, length),
            EUnit("cm", 1.0e-2, length),
            EUnit("m", 1.0, length),
            EUnit("km", 1.0e3, length),
            EUnit("m2", 1.0, area),
            EUnit("ha", 1.0e4, area),
            EUnit("km2", 1.0e6, area),
            EUnit("m3", 1.0, volume),
            EUnit("l", 1.0e-3, volume),
            EUnit("cl", 1.0e-5, volume),
            EUnit("ml", 1.0e-6, volume),
            EUnit("Bq", 1.0, radioactivity),
            EUnit("kBq", 1000.0, radioactivity),
            EUnit("s", 1.0, time),
            EUnit("min", 60.0, time),
            EUnit("hour", 3600.0, time),
            EUnit("day", 24 * 3600.0, time),
            EUnit("Wh", 1.0, energy),
            EUnit("kWh", 1.0e3, energy),
            EUnit("MWh", 1.0e6, energy),
            EUnit("J", 1.0 / 3600.0, energy),
            EUnit("kJ", 1.0e3 / 3600.0, energy),
            EUnit("MJ", 1.0e6 / 3600.0, energy),
            EUnit("W", 1.0, power),
        ).associateBy { it.symbol }

        val packages = mapOf(
            Pair(
                "prelude.units",
                Package(
                    "prelude.units",
                    emptyList(),
                    Environment.of(definitions),
                )
            )
        )
    }
}
