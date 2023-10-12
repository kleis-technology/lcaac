package ch.kleis.lcaac.core.prelude

import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral


class Prelude {
    sealed interface UnitDef<Q> {
        fun ref(): String
        val value: EUnitLiteral<Q>
    }

    data class PrimitiveUnitDef<Q>(
        override val value: EUnitLiteral<Q>
    ) : UnitDef<Q> {
        override fun ref(): String {
            return sanitize(value.symbol.toString(), toLowerCase = false)
        }
    }

    data class CompositeUnitDef<Q>(
        override val value: EUnitLiteral<Q>,
        val rawAlias: String,
    ) : UnitDef<Q> {
        override fun ref(): String {
            return sanitize(value.symbol.toString(), toLowerCase = false)
        }
    }

    companion object {
        const val PKG_NAME = "builtin_units"

        // primitive dimensions
        private val mass = Dimension.of("mass")
        private val length = Dimension.of("length")
        private val temperature = Dimension.of("temperature")
        private val energy = Dimension.of("energy")
        private val time = Dimension.of("time")
        private val radioactivity = Dimension.of("radioactivity")
        private val luminousIntensity = Dimension.of("luminous_intensity")

        private fun <Q> molHpEq(): PrimitiveUnitDef<Q> = PrimitiveUnitDef(
            EUnitLiteral(
                UnitSymbol.of("mol H+-Eq"),
                1.0,
                Dimension.of("accumulated exceedance (AE)")
            )
        )
        fun <Q> primitiveUnits(): Map<String, PrimitiveUnitDef<Q>> = listOf<PrimitiveUnitDef<Q>>(
            PrimitiveUnitDef(EUnitLiteral(UnitSymbol.of("u"), 1.0, none)),
            PrimitiveUnitDef(EUnitLiteral(UnitSymbol.of("kg"), 1.0, mass)),
            PrimitiveUnitDef(EUnitLiteral(UnitSymbol.of("m"), 1.0, length)),
            PrimitiveUnitDef(EUnitLiteral(UnitSymbol.of("K"), 1.0, temperature)),
            PrimitiveUnitDef(EUnitLiteral(UnitSymbol.of("Wh"), 1.0, energy)),
            PrimitiveUnitDef(EUnitLiteral(UnitSymbol.of("s"), 1.0, time)),
            PrimitiveUnitDef(EUnitLiteral(UnitSymbol.of("Bq"), 1.0, radioactivity)),
            PrimitiveUnitDef(EUnitLiteral(UnitSymbol.of("lumen"), 1.0, luminousIntensity)),
            molHpEq(),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("kg CO2-Eq"),
                    1.0,
                    Dimension.of("global warming potential (GWP100)")
                )
            ),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("CTUe"),
                    1.0,
                    Dimension.of("comparative toxic unit for ecosystems (CTUe)")
                )
            ),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("MJ, net calorific value"),
                    1.0,
                    Dimension.of("abiotic depletion potential (ADP): fossil fuels")
                )
            ),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("kg P-Eq"),
                    1.0,
                    Dimension.of("fraction of nutrients reaching freshwater end compartment (P)")
                )
            ),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("kg N-Eq"),
                    1.0,
                    Dimension.of("fraction of nutrients reaching marine end compartment (N)")
                )
            ),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("CTUh"),
                    1.0,
                    Dimension.of("comparative toxic unit for human (CTUh)")
                )
            ),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("kBq U235-Eq"),
                    1.0,
                    Dimension.of("human exposure efficiency relative to u235")
                )
            ),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("kg Sb-Eq"),
                    1.0,
                    Dimension.of("abiotic depletion potential (ADP): elements (ultimate reserves)")
                )
            ),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("kg CFC-11-Eq"),
                    1.0,
                    Dimension.of("ozone depletion potential (ODP)")
                )
            ),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("disease incidence"),
                    1.0,
                    Dimension.of("impact on human health")
                )
            ),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("kg NMVOC-Eq"),
                    1.0,
                    Dimension.of("tropospheric ozone concentration increase")
                )
            ),
            PrimitiveUnitDef(
                EUnitLiteral(
                    UnitSymbol.of("m3 world eq. deprived"),
                    1.0,
                    Dimension.of("user deprivation potential (deprivation-weighted water consumption)"),
                )
            ),
        ).associateBy { it.ref() }

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

        fun <Q> compositeUnits(): Map<String, CompositeUnitDef<Q>> = listOf<CompositeUnitDef<Q>>(
            // dimensionless
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("dimensionless"), 1.0, none), "1 u"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("piece"), 1.0, none), "1 u"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("person"), 1.0, none), "1 u"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("p"), 1.0, none), "1 u"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("percent"), 1.0e-2, none), "1e-2 u"),

            // temperature

            // mass
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("ton"), 1E3, mass), "1e3 kg"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("g"), 1.0e-3, mass), "1e-3 kg"),

            // length
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("mm"), 1.0e-3, length), "1e-3 m"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("cm"), 1.0e-2, length), "1e-2 m"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("km"), 1.0e3, length), "1e3 m"),

            // area
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("m2"), 1.0, area), "m^2"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("ha"), 1.0e4, area), "1e4 m2"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("km2"), 1.0e6, area), "1e6 m2"),

            // volume
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("m3"), 1.0, volume), "m^3"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("l"), 1.0e-3, volume), "1e-3 m3"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("cl"), 1.0e-5, volume), "1e-5 m3"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("ml"), 1.0e-6, volume), "1e-6 m3"),

            // radioactivity
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("kBq"), 1000.0, radioactivity), "1e3 Bq"),

            // time
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("min"), 60.0, time), "60 s"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("hour"), 3600.0, time), "60 min"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("day"), 24 * 3600.0, time), "24 hour"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("year"), 365 * 24 * 3600.0, time), "365 day"),

            // energy
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("kWh"), 1.0e3, energy), "1e3 Wh"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("MWh"), 1.0e6, energy), "1e3 kWh"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("J"), 1.0 / 3600.0, energy), "1 Wh / 3600 u"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("kJ"), 1.0e3 / 3600.0, energy), "1e3 J"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("MJ"), 1.0e6 / 3600.0, energy), "1e6 J"),

            // power
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("W"), 1.0 / 3600.0, power), "1 J / s"),

            // land_occupation
            CompositeUnitDef(
                EUnitLiteral(UnitSymbol.of("m2a"), 1.0 * 365 * 24 * 3600, land_occupation),
                "1 m2 * 1 year"
            ),

            // illuminance
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("lux"), 1.0, illuminance), "1 lumen / m2"),

            // other
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("tkm"), 1e3 * 1e3, transport), "1 ton * km"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("my"), 365 * 24 * 3600.0, length_time), "1 m * year"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("personkm"), 1000.0, person_distance), "1 p * km"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("kgy"), 365 * 24 * 3600.0, mass_time), "1 kg * year"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("kga"), 365 * 24 * 3600.0, mass_time), "1 kg * year"),
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("m3y"), 365 * 24 * 3600.0, volume_time), "1 m3 * year"),

            // accumulated exceedance
            CompositeUnitDef(EUnitLiteral(UnitSymbol.of("mol N-Eq"), 1.0, Dimension.of("accumulated exceedance (AE)")), "1 ${molHpEq<Q>().ref()}"),
        ).associateBy { it.ref() }


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
