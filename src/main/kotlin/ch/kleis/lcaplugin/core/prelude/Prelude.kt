package ch.kleis.lcaplugin.core.prelude

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.expression.DataExpression
import ch.kleis.lcaplugin.core.lang.expression.EUnitLiteral


class Prelude {
    companion object {
        val mass = Dimension.of("mass")
        val length = Dimension.of("length")
        private val temperature = Dimension.of("temperature")
        val area = length.multiply(length)
        val volume = length.multiply(area)
        val energy = Dimension.of("energy")
        val time = Dimension.of("time")
        val land_occupation = area.multiply(time)
        val transport = mass.multiply(length)
        val power = energy.divide(time)
        val none = Dimension.None
        val radioactivity = Dimension.of("radioactivity")
        val length_time = length.multiply(time)
        val person_distance = none.multiply(length)
        val mass_time = mass.multiply(time)
        val volume_time = volume.multiply(time)

        private val primitiveDimensionMap = listOf(
            mass,
            length,
            temperature,
            energy,
            time,
            radioactivity,
        ).associateBy { it.toString() }

        fun <Q> unitWithCompositeDimensionWithoutReferenceUnit(): Map<EUnitLiteral<Q>, String> = mapOf(
            EUnitLiteral<Q>(UnitSymbol.of("tkm"), 1e3 * 1e3, transport) to "1 ton * km",
            EUnitLiteral<Q>(UnitSymbol.of("my"), 365 * 24 * 3600.0, length_time) to "1 m * year",
            EUnitLiteral<Q>(UnitSymbol.of("personkm"), 1000.0, person_distance) to "1 p * km",
            EUnitLiteral<Q>(UnitSymbol.of("kgy"), 365 * 24 * 3600.0, mass_time) to "1 kg * year",
            EUnitLiteral<Q>(UnitSymbol.of("kga"), 365 * 24 * 3600.0, mass_time) to "1 kg * year",
            EUnitLiteral<Q>(UnitSymbol.of("m3y"), 365 * 24 * 3600.0, volume_time) to "1 m3 * year",
        )

        fun <Q> unitWithDimensionWithReferenceUnit(): Map<String, EUnitLiteral<Q>> = listOf(
            EUnitLiteral(UnitSymbol.of("u"), 1.0, none),
            EUnitLiteral<Q>(UnitSymbol.of("dimensionless"), 1.0, none),
            EUnitLiteral<Q>(UnitSymbol.of("piece"), 1.0, none),
            EUnitLiteral<Q>(UnitSymbol.of("person"), 1.0, none),
            EUnitLiteral<Q>(UnitSymbol.of("p"), 1.0, none),
            EUnitLiteral<Q>(UnitSymbol.of("percent"), 1.0e-2, none),
            EUnitLiteral<Q>(UnitSymbol.of("K"), 1.0, temperature),
            EUnitLiteral<Q>(UnitSymbol.of("kg"), 1.0, mass),
            EUnitLiteral<Q>(UnitSymbol.of("ton"), 1E3, mass),
            EUnitLiteral<Q>(UnitSymbol.of("g"), 1.0e-3, mass),
            EUnitLiteral<Q>(UnitSymbol.of("mm"), 1.0e-3, length),
            EUnitLiteral<Q>(UnitSymbol.of("cm"), 1.0e-2, length),
            EUnitLiteral<Q>(UnitSymbol.of("m"), 1.0, length),
            EUnitLiteral<Q>(UnitSymbol.of("km"), 1.0e3, length),
            EUnitLiteral<Q>(UnitSymbol.of("m2"), 1.0, area),
            EUnitLiteral<Q>(UnitSymbol.of("ha"), 1.0e4, area),
            EUnitLiteral<Q>(UnitSymbol.of("km2"), 1.0e6, area),
            EUnitLiteral<Q>(UnitSymbol.of("m3"), 1.0, volume),
            EUnitLiteral<Q>(UnitSymbol.of("Sm3"), 1.0, volume),
            EUnitLiteral<Q>(UnitSymbol.of("l"), 1.0e-3, volume),
            EUnitLiteral<Q>(UnitSymbol.of("cl"), 1.0e-5, volume),
            EUnitLiteral<Q>(UnitSymbol.of("ml"), 1.0e-6, volume),
            EUnitLiteral<Q>(UnitSymbol.of("Bq"), 1.0, radioactivity),
            EUnitLiteral<Q>(UnitSymbol.of("kBq"), 1000.0, radioactivity),
            EUnitLiteral<Q>(UnitSymbol.of("s"), 1.0, time),
            EUnitLiteral<Q>(UnitSymbol.of("min"), 60.0, time),
            EUnitLiteral<Q>(UnitSymbol.of("hour"), 3600.0, time),
            EUnitLiteral<Q>(UnitSymbol.of("day"), 24 * 3600.0, time),
            EUnitLiteral<Q>(UnitSymbol.of("year"), 365 * 24 * 3600.0, time),
            EUnitLiteral<Q>(UnitSymbol.of("Wh"), 1.0, energy),
            EUnitLiteral<Q>(UnitSymbol.of("kWh"), 1.0e3, energy),
            EUnitLiteral<Q>(UnitSymbol.of("MWh"), 1.0e6, energy),
            EUnitLiteral<Q>(UnitSymbol.of("J"), 1.0 / 3600.0, energy),
            EUnitLiteral<Q>(UnitSymbol.of("kJ"), 1.0e3 / 3600.0, energy),
            EUnitLiteral<Q>(UnitSymbol.of("MJ"), 1.0e6 / 3600.0, energy),
            EUnitLiteral<Q>(UnitSymbol.of("W"), 1.0 / 3600.0, power),
            EUnitLiteral<Q>(UnitSymbol.of("m2a"), 1.0 * 365 * 24 * 3600, land_occupation),
        ).associateBy { it.symbol.toString() }

        fun <Q> unitMap(): Map<String, EUnitLiteral<Q>> =
            unitWithCompositeDimensionWithoutReferenceUnit<Q>().keys
                .associateBy { it.symbol.toString() } + unitWithDimensionWithReferenceUnit()

        fun <Q> units(): Register<DataExpression<Q>> = Register.from(unitMap())

    }
}
