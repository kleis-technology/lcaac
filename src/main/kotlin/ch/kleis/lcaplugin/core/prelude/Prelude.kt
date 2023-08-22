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
        val primitiveDimensions = Register.from(primitiveDimensionMap)

        fun <Q> unitMap(): Map<String, EUnitLiteral<Q>> = listOf(
            EUnitLiteral<Q>(UnitSymbol.of("u"), 1.0, none),
            EUnitLiteral(UnitSymbol.of("dimensionless"), 1.0, none),
            EUnitLiteral(UnitSymbol.of("piece"), 1.0, none),
            EUnitLiteral(UnitSymbol.of("person"), 1.0, none),
            EUnitLiteral(UnitSymbol.of("p"), 1.0, none),
            EUnitLiteral(UnitSymbol.of("percent"), 1.0e-2, none),
            EUnitLiteral(UnitSymbol.of("K"), 1.0, temperature),
            EUnitLiteral(UnitSymbol.of("kg"), 1.0, mass),
            EUnitLiteral(UnitSymbol.of("ton"), 1E3, mass),
            EUnitLiteral(UnitSymbol.of("g"), 1.0e-3, mass),
            EUnitLiteral(UnitSymbol.of("mm"), 1.0e-3, length),
            EUnitLiteral(UnitSymbol.of("cm"), 1.0e-2, length),
            EUnitLiteral(UnitSymbol.of("m"), 1.0, length),
            EUnitLiteral(UnitSymbol.of("km"), 1.0e3, length),
            EUnitLiteral(UnitSymbol.of("m2"), 1.0, area),
            EUnitLiteral(UnitSymbol.of("ha"), 1.0e4, area),
            EUnitLiteral(UnitSymbol.of("km2"), 1.0e6, area),
            EUnitLiteral(UnitSymbol.of("m3"), 1.0, volume),
            EUnitLiteral(UnitSymbol.of("Sm3"), 1.0, volume),
            EUnitLiteral(UnitSymbol.of("l"), 1.0e-3, volume),
            EUnitLiteral(UnitSymbol.of("cl"), 1.0e-5, volume),
            EUnitLiteral(UnitSymbol.of("ml"), 1.0e-6, volume),
            EUnitLiteral(UnitSymbol.of("Bq"), 1.0, radioactivity),
            EUnitLiteral(UnitSymbol.of("kBq"), 1000.0, radioactivity),
            EUnitLiteral(UnitSymbol.of("s"), 1.0, time),
            EUnitLiteral(UnitSymbol.of("min"), 60.0, time),
            EUnitLiteral(UnitSymbol.of("hour"), 3600.0, time),
            EUnitLiteral(UnitSymbol.of("day"), 24 * 3600.0, time),
            EUnitLiteral(UnitSymbol.of("year"), 365 * 24 * 3600.0, time),
            EUnitLiteral(UnitSymbol.of("Wh"), 1.0, energy),
            EUnitLiteral(UnitSymbol.of("kWh"), 1.0e3, energy),
            EUnitLiteral(UnitSymbol.of("MWh"), 1.0e6, energy),
            EUnitLiteral(UnitSymbol.of("J"), 1.0 / 3600.0, energy),
            EUnitLiteral(UnitSymbol.of("kJ"), 1.0e3 / 3600.0, energy),
            EUnitLiteral(UnitSymbol.of("MJ"), 1.0e6 / 3600.0, energy),
            EUnitLiteral(UnitSymbol.of("W"), 1.0 / 3600.0, power),
            EUnitLiteral(UnitSymbol.of("m2a"), 1.0 * 365 * 24 * 3600, land_occupation),
            EUnitLiteral(UnitSymbol.of("__kgm"), 1.0, transport),
            EUnitLiteral(UnitSymbol.of("tkm"), 1e3 * 1e3, transport),
            EUnitLiteral(UnitSymbol.of("__meterseconde"), 1.0, length_time),
            EUnitLiteral(UnitSymbol.of("my"), 365 * 24 * 3600.0, length_time),
            EUnitLiteral(UnitSymbol.of("__personm"), 1.0, person_distance),
            EUnitLiteral(UnitSymbol.of("personkm"), 1000.0, person_distance),
            EUnitLiteral(UnitSymbol.of("__kgs"), 1.0, mass_time),
            EUnitLiteral(UnitSymbol.of("kgy"), 365 * 24 * 3600.0, mass_time),
            EUnitLiteral(UnitSymbol.of("kga"), 365 * 24 * 3600.0, mass_time),
            EUnitLiteral(UnitSymbol.of("__m3seconde"), 1.0, volume_time),
            EUnitLiteral(UnitSymbol.of("m3y"), 365 * 24 * 3600.0, volume_time),
        ).associateBy { it.symbol.toString() }

        fun <Q> units(): Register<DataExpression<Q>> = Register.from(unitMap())
    }
}
