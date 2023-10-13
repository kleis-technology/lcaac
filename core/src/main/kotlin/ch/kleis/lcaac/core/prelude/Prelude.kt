package ch.kleis.lcaac.core.prelude

import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral


class Prelude {
    data class UnitDef<Q>(
        val ref: String,
        val value: EUnitLiteral<Q>,
        val rawAlias: String?,
    ) {
        constructor(value: EUnitLiteral<Q>) : this(
            sanitize(value.symbol.toString(), toLowerCase = false), value, null
        )
        constructor(value: EUnitLiteral<Q>, rawAlias: String): this(
            sanitize(value.symbol.toString(), toLowerCase = false), value, rawAlias
        )
    }


    companion object {
        const val PKG_NAME = "builtin_units"
        private fun <Q> pud(symbol: String, dimension: Dimension): UnitDef<Q> =
            UnitDef(EUnitLiteral(UnitSymbol.of(symbol), 1.0, dimension))

        private fun <Q> pud(symbol: String, dimension: String): UnitDef<Q> =
            pud(symbol, Dimension.of(dimension))

        private fun <Q> cud(
            symbol: String,
            scale: Double,
            dimension: Dimension,
            rawAlias: String
        ): UnitDef<Q> =
            UnitDef(EUnitLiteral(UnitSymbol.of(symbol), scale, dimension), rawAlias)

        // primitive dimensions
        private val mass = Dimension.of("mass")
        private val length = Dimension.of("length")
        private val temperature = Dimension.of("temperature")
        private val energy = Dimension.of("energy")
        private val time = Dimension.of("time")
        private val radioactivity = Dimension.of("radioactivity")
        private val luminousIntensity = Dimension.of("luminous_intensity")

        private fun <Q> molHpEq(): UnitDef<Q> = pud("mol H+-Eq", "accumulated exceedance (AE)")
        fun <Q> primitiveUnits(): Map<String, UnitDef<Q>> = listOf<UnitDef<Q>>(
            pud("u", none),
            pud("kg", mass),
            pud("m", length),
            pud("K", temperature),
            pud("Wh", energy),
            pud("s", time),
            pud("Bq", radioactivity),
            pud("lumen", luminousIntensity),
            molHpEq(),
            pud("mol H+-Eq", "accumulated exceedance (AE)"),
            pud("kg CO2-Eq", "global warming potential (GWP100)"),
            pud("kg CO2-Eq", "global warming potential (GWP100)"),
            pud("kg CO2-Eq", "global warming potential (GWP100)"),
            pud("kg CO2-Eq", "global warming potential (GWP100)"),
            pud("CTUe", "comparative toxic unit for ecosystems (CTUe)"),
            pud("CTUe", "comparative toxic unit for ecosystems (CTUe)"),
            pud("CTUe", "comparative toxic unit for ecosystems (CTUe)"),
            pud("MJ, net calorific value", "abiotic depletion potential (ADP): fossil fuels"),
            pud("kg P-Eq", "fraction of nutrients reaching freshwater end compartment (P)"),
            pud("kg N-Eq", "fraction of nutrients reaching marine end compartment (N)"),
            pud("mol N-Eq", "accumulated exceedance (AE)"),
            pud("CTUh", "comparative toxic unit for human (CTUh)"),
            pud("CTUh", "comparative toxic unit for human (CTUh)"),
            pud("CTUh", "comparative toxic unit for human (CTUh)"),
            pud("CTUh", "comparative toxic unit for human (CTUh)"),
            pud("CTUh", "comparative toxic unit for human (CTUh)"),
            pud("CTUh", "comparative toxic unit for human (CTUh)"),
            pud("kBq U235-Eq", "human exposure efficiency relative to u235"),
            pud("kg Sb-Eq", "abiotic depletion potential (ADP): elements (ultimate reserves)"),
            pud("kg CFC-11-Eq", "ozone depletion potential (ODP)"),
            pud("disease incidence", "impact on human health"),
            pud("kg NMVOC-Eq", "tropospheric ozone concentration increase"),
            pud("m3 world eq. deprived", "user deprivation potential (deprivation-weighted water consumption)"),
        ).associateBy { it.ref }

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
        private val illuminance = luminousIntensity.divide(area)

        fun <Q> compositeUnits(): Map<String, UnitDef<Q>> = listOf<UnitDef<Q>>(
            // dimensionless
            cud("dimensionless", 1.0, none, "1 u"),
            cud("piece", 1.0, none, "1 u"),
            cud("guest night", 1.0, none, "1 u"),
            cud("person", 1.0, none, "1 u"),
            cud("p", 1.0, none, "1 u"),
            cud("percent", 1.0e-2, none, "1e-2 u"),

            // temperature

            // mass
            cud("ton", 1E3, mass, "1e3 kg"),
            cud("g", 1.0e-3, mass, "1e-3 kg"),

            // length
            cud("mm", 1.0e-3, length, "1e-3 m"),
            cud("cm", 1.0e-2, length, "1e-2 m"),
            cud("km", 1.0e3, length, "1e3 m"),

            // area
            UnitDef(EUnitLiteral(UnitSymbol.of("m2"), 1.0, area), "m^2"),
            cud("ha", 1.0e4, area, "1e4 m2"),
            UnitDef(EUnitLiteral(UnitSymbol.of("km2"), 1.0e6, area), "1e6 m2"),

            // volume
            UnitDef(EUnitLiteral(UnitSymbol.of("m3"), 1.0, volume), "m^3"),
            cud("l", 1.0e-3, volume, "1e-3 m3"),
            cud("cl", 1.0e-5, volume, "1e-5 m3"),
            cud("ml", 1.0e-6, volume, "1e-6 m3"),

            // radioactivity
            cud("kBq", 1000.0, radioactivity, "1e3 Bq"),

            // time
            cud("min", 60.0, time, "60 s"),
            cud("hour", 3600.0, time, "60 min"),
            UnitDef(EUnitLiteral(UnitSymbol.of("day"), 24 * 3600.0, time), "24 hour"),
            UnitDef(EUnitLiteral(UnitSymbol.of("year"), 365 * 24 * 3600.0, time), "365 day"),

            // energy
            cud("kWh", 1.0e3, energy, "1e3 Wh"),
            cud("MWh", 1.0e6, energy, "1e3 kWh"),
            cud("J", 1.0 / 3600.0, energy, "1 Wh / 3600 u"),
            cud("kJ", 1.0e3 / 3600.0, energy, "1e3 J"),
            cud("MJ", 1.0e6 / 3600.0, energy, "1e6 J"),

            // power
            cud("W", 1.0 / 3600.0, power, "1 J / s"),

            // land_occupation
            cud("m2a", 1.0 * 365 * 24 * 3600, land_occupation, "1 m2 * 1 year"),

            // illuminance
            cud("lux", 1.0, illuminance, "1 lumen / m2"),

            // other
            cud("tkm", 1e3 * 1e3, transport, "1 ton * km"),
            cud("my", 365 * 24 * 3600.0, length_time, "1 m * year"),
            cud("personkm", 1000.0, person_distance, "1 p * km"),
            cud("kgy", 365 * 24 * 3600.0, mass_time, "1 kg * year"),
            cud("kga", 365 * 24 * 3600.0, mass_time, "1 kg * year"),
            cud("m3y", 365 * 24 * 3600.0, volume_time, "1 m3 * year"),

            // accumulated exceedance
            cud("mol N-Eq", 1.0, Dimension.of("accumulated exceedance (AE)"), "1 ${molHpEq<Q>().ref}"),
        ).associateBy { it.ref }


        fun <Q> unitMap(): Map<String, EUnitLiteral<Q>> = (compositeUnits<Q>() + primitiveUnits())
            .mapValues { it.value.value }

        private fun primitiveDimensionMap(): Map<String, Dimension> =
            primitiveUnits<Any>().mapValues { it.value.value.dimension }

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
