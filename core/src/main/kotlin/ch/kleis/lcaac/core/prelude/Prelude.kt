package ch.kleis.lcaac.core.prelude

import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral


class Prelude {
    companion object {
        const val PKG_NAME = "builtin_units"

        // primitive dimensions
        private val mass = Dimension.of("mass")
        private val length = Dimension.of("length")
        private val temperature = Dimension.of("temperature")
        private val energy = Dimension.of("energy")
        private val time = Dimension.of("time")
        private val radioactivity = Dimension.of("radioactivity")
        private val luminous_intensity = Dimension.of("luminous_intensity")

        fun <Q> primitiveUnits(): Map<String, EUnitLiteral<Q>> = listOf(
            EUnitLiteral<Q>(UnitSymbol.of("u"), 1.0, none),
            EUnitLiteral(UnitSymbol.of("kg"), 1.0, mass),
            EUnitLiteral(UnitSymbol.of("m"), 1.0, length),
            EUnitLiteral(UnitSymbol.of("K"), 1.0, temperature),
            EUnitLiteral(UnitSymbol.of("Wh"), 1.0, energy),
            EUnitLiteral(UnitSymbol.of("s"), 1.0, time),
            EUnitLiteral(UnitSymbol.of("Bq"), 1.0, radioactivity),
            EUnitLiteral(UnitSymbol.of("lumen"), 1.0, luminous_intensity),

            EUnitLiteral(UnitSymbol.of("mol H+-Eq"), 1.0, Dimension.of("accumulated exceedance (AE)")),
            EUnitLiteral(UnitSymbol.of("kg CO2-Eq"), 1.0, Dimension.of("global warming potential (GWP100)")),
            EUnitLiteral(UnitSymbol.of("CTUe"), 1.0, Dimension.of("comparative toxic unit for ecosystems (CTUe)")),
            EUnitLiteral(
                UnitSymbol.of("MJ, net calorific value"),
                1.0,
                Dimension.of("abiotic depletion potential (ADP): fossil fuels")
            ),
            EUnitLiteral(
                UnitSymbol.of("kg P-Eq"),
                1.0,
                Dimension.of("fraction of nutrients reaching freshwater end compartment (P)")
            ),
            EUnitLiteral(
                UnitSymbol.of("kg N-Eq"),
                1.0,
                Dimension.of("fraction of nutrients reaching marine end compartment (N)")
            ),
            EUnitLiteral(UnitSymbol.of("mol N-Eq"), 1.0, Dimension.of("accumulated exceedance (AE)")),
            EUnitLiteral(UnitSymbol.of("CTUh"), 1.0, Dimension.of("comparative toxic unit for human (CTUh)")),
            EUnitLiteral(UnitSymbol.of("kBq U235-Eq"), 1.0, Dimension.of("human exposure efficiency relative to u235")),
            EUnitLiteral(UnitSymbol.of("dimensionless"), 1.0, Dimension.of("soil quality index")),
            EUnitLiteral(
                UnitSymbol.of("kg Sb-Eq"),
                1.0,
                Dimension.of("abiotic depletion potential (ADP): elements (ultimate reserves)")
            ),
            EUnitLiteral(UnitSymbol.of("kg CFC-11-Eq"), 1.0, Dimension.of("ozone depletion potential (ODP)")),
            EUnitLiteral(UnitSymbol.of("disease incidence"), 1.0, Dimension.of("impact on human health")),
            EUnitLiteral(UnitSymbol.of("kg NMVOC-Eq"), 1.0, Dimension.of("tropospheric ozone concentration increase")),
            EUnitLiteral(
                UnitSymbol.of("m3 world eq. deprived"),
                1.0,
                Dimension.of("user deprivation potential (deprivation-weighted water consumption)")
            ),
        ).associateBy { sanitize(it.symbol.toString(), toLowerCase = false) }

        // composite dimensions
        private val area = length.multiply(length)
        private val volume = length.multiply(area)
        private val land_occupation = area.multiply(time)
        private val transport = mass.multiply(length)
        private val power = energy.divide(time)
        private val none = Dimension.None
        private val length_time = length.multiply(time)
        private val person_distance = none.multiply(length)
        private val mass_time = mass.multiply(time)
        private val volume_time = volume.multiply(time)
        private val illuminance = luminous_intensity.divide(area)

        fun <Q> compositeUnits(): Map<EUnitLiteral<Q>, String> = mapOf(
            // dimensionless
            EUnitLiteral<Q>(UnitSymbol.of("dimensionless"), 1.0, none) to "1 u",
            EUnitLiteral<Q>(UnitSymbol.of("piece"), 1.0, none) to "1 u",
            EUnitLiteral<Q>(UnitSymbol.of("person"), 1.0, none) to "1 u",
            EUnitLiteral<Q>(UnitSymbol.of("p"), 1.0, none) to "1 u",
            EUnitLiteral<Q>(UnitSymbol.of("percent"), 1.0e-2, none) to "1e-2 u",

            // temperature

            // mass
            EUnitLiteral<Q>(UnitSymbol.of("ton"), 1E3, mass) to "1e3 kg",
            EUnitLiteral<Q>(UnitSymbol.of("g"), 1.0e-3, mass) to "1e-3 kg",

            // length
            EUnitLiteral<Q>(UnitSymbol.of("mm"), 1.0e-3, length) to "1e-3 m",
            EUnitLiteral<Q>(UnitSymbol.of("cm"), 1.0e-2, length) to "1e-2 m",
            EUnitLiteral<Q>(UnitSymbol.of("km"), 1.0e3, length) to "1e3 m",

            // area
            EUnitLiteral<Q>(UnitSymbol.of("m2"), 1.0, area) to "m^2",
            EUnitLiteral<Q>(UnitSymbol.of("ha"), 1.0e4, area) to "1e4 m2",
            EUnitLiteral<Q>(UnitSymbol.of("km2"), 1.0e6, area) to "1e6 m2",

            // volume
            EUnitLiteral<Q>(UnitSymbol.of("m3"), 1.0, volume) to "m^3",
            EUnitLiteral<Q>(UnitSymbol.of("l"), 1.0e-3, volume) to "1e-3 m3",
            EUnitLiteral<Q>(UnitSymbol.of("cl"), 1.0e-5, volume) to "1e-5 m3",
            EUnitLiteral<Q>(UnitSymbol.of("ml"), 1.0e-6, volume) to "1e-6 m3",

            // radioactivity
            EUnitLiteral<Q>(UnitSymbol.of("kBq"), 1000.0, radioactivity) to "1e3 Bq",

            // time
            EUnitLiteral<Q>(UnitSymbol.of("min"), 60.0, time) to "60 s",
            EUnitLiteral<Q>(UnitSymbol.of("hour"), 3600.0, time) to "60 min",
            EUnitLiteral<Q>(UnitSymbol.of("day"), 24 * 3600.0, time) to "24 hour",
            EUnitLiteral<Q>(UnitSymbol.of("year"), 365 * 24 * 3600.0, time) to "365 day",

            // energy
            EUnitLiteral<Q>(UnitSymbol.of("kWh"), 1.0e3, energy) to "1e3 Wh",
            EUnitLiteral<Q>(UnitSymbol.of("MWh"), 1.0e6, energy) to "1e3 kWh",
            EUnitLiteral<Q>(UnitSymbol.of("J"), 1.0 / 3600.0, energy) to "1 Wh / 3600 u",
            EUnitLiteral<Q>(UnitSymbol.of("kJ"), 1.0e3 / 3600.0, energy) to "1e3 J",
            EUnitLiteral<Q>(UnitSymbol.of("MJ"), 1.0e6 / 3600.0, energy) to "1e6 J",

            // power
            EUnitLiteral<Q>(UnitSymbol.of("W"), 1.0 / 3600.0, power) to "1 J / s",

            // land_occupation
            EUnitLiteral<Q>(UnitSymbol.of("m2a"), 1.0 * 365 * 24 * 3600, land_occupation) to "1 m2 * 1 year",

            // illuminance
            EUnitLiteral<Q>(UnitSymbol.of("lux"), 1.0, illuminance) to "1 lumen / m2",

            // other
            EUnitLiteral<Q>(UnitSymbol.of("tkm"), 1e3 * 1e3, transport) to "1 ton * km",
            EUnitLiteral<Q>(UnitSymbol.of("my"), 365 * 24 * 3600.0, length_time) to "1 m * year",
            EUnitLiteral<Q>(UnitSymbol.of("personkm"), 1000.0, person_distance) to "1 p * km",
            EUnitLiteral<Q>(UnitSymbol.of("kgy"), 365 * 24 * 3600.0, mass_time) to "1 kg * year",
            EUnitLiteral<Q>(UnitSymbol.of("kga"), 365 * 24 * 3600.0, mass_time) to "1 kg * year",
            EUnitLiteral<Q>(UnitSymbol.of("m3y"), 365 * 24 * 3600.0, volume_time) to "1 m3 * year",
        )


        fun <Q> unitMap(): Map<String, EUnitLiteral<Q>> =
            compositeUnits<Q>().keys
                .associateBy { it.symbol.toString() } + primitiveUnits()

        private fun primitiveDimensionMap(): Map<String, Dimension> =
            primitiveUnits<Any>().mapValues { it.value.dimension }

        fun <Q> units(): Register<DataExpression<Q>> = Register.from(unitMap())
        fun dimensions(): Register<Dimension> = Register.from(primitiveDimensionMap())

        fun sanitize(s: String, toLowerCase: Boolean = true): String {
            if (s.isBlank()) {
                return s
            }

            val r = if (s[0].isDigit()) "_$s" else s
            val spaces = """\s+""".toRegex()
            val nonAlphaNumeric = """[^a-zA-Z0-9_]+""".toRegex()
            val underscores = Regex("_{2,}")

            return r.let {
                if (toLowerCase) it.lowercase()
                else it
            }.trim()
                .replace(spaces, "_")
                .replace("*", "_m_")
                .replace("+", "_p_")
                .replace("&", "_a_")
                .replace(">", "_gt_")
                .replace("<", "_lt_")
                .replace("/", "_sl_")
                .replace(nonAlphaNumeric, "_")
                .replace(underscores, "_")
                .trimEnd('_')
        }
    }
}
