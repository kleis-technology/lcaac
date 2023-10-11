package ch.kleis.lcaac.core.prelude

import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral


class Prelude {
    companion object {
        const val pkgName = "builtin_units"

        // primitive dimensions
        val mass = Dimension.of("mass")
        val length = Dimension.of("length")
        private val temperature = Dimension.of("temperature")
        val energy = Dimension.of("energy")
        val time = Dimension.of("time")
        val radioactivity = Dimension.of("radioactivity")
        val luminous_intensity = Dimension.of("luminous_intensity")

        val acidification = Dimension.of("acidification")
        val climateChange = Dimension.of("climate_change")
        val ecotoxicity = Dimension.of("ecotoxicity")
        val particulateMatterFormation = Dimension.of("particulate_matter_formation")
        val eutrophicationMarine = Dimension.of("eutrophication_marine")
        val eutrophicationFreshwater = Dimension.of("eutrophication_freshwater")
        val eutrophicationTerrestrial = Dimension.of("eutrophication_terrestrial")
        val humanToxicity = Dimension.of("human_toxicity")
        val ionisingRadiation = Dimension.of("ionising_radiation")
        val ozoneDepletion = Dimension.of("ozone_depletion")
        val resourceUseFossils = Dimension.of("resource_use_fossils")
        val resourceUseMineralsAndMetals = Dimension.of("resource_use_minerals_and_metals")
        val waterUse = Dimension.of("water_use")


        fun <Q> primitiveUnits(): Map<String, EUnitLiteral<Q>> = listOf(
            EUnitLiteral<Q>(UnitSymbol.of("u"), 1.0, none),
            EUnitLiteral(UnitSymbol.of("kg"), 1.0, mass),
            EUnitLiteral(UnitSymbol.of("m"), 1.0, length),
            EUnitLiteral(UnitSymbol.of("K"), 1.0, temperature),
            EUnitLiteral(UnitSymbol.of("Wh"), 1.0, energy),
            EUnitLiteral(UnitSymbol.of("s"), 1.0, time),
            EUnitLiteral(UnitSymbol.of("Bq"), 1.0, radioactivity),
            EUnitLiteral(UnitSymbol.of("lumen"), 1.0, luminous_intensity),

            EUnitLiteral(UnitSymbol.of("mol_H_p_Eq"), 1.0, acidification),
            EUnitLiteral(UnitSymbol.of("kg_CO2_Eq"), 1.0, climateChange),
            EUnitLiteral(UnitSymbol.of("CTUe"), 1.0, ecotoxicity),
            EUnitLiteral(UnitSymbol.of("disease_incidence"), 1.0, particulateMatterFormation),
            EUnitLiteral(UnitSymbol.of("kg_N_Eq"), 1.0, eutrophicationMarine),
            EUnitLiteral(UnitSymbol.of("kg_P_Eq"), 1.0, eutrophicationFreshwater),
            EUnitLiteral(UnitSymbol.of("mol_N_Eq"), 1.0, eutrophicationTerrestrial),
            EUnitLiteral(UnitSymbol.of("CTUh"), 1.0, humanToxicity),
            EUnitLiteral(UnitSymbol.of("kBq_U235_Eq"), 1.0, ionisingRadiation),
            EUnitLiteral(UnitSymbol.of("kg_CFC_11_Eq"), 1.0, ozoneDepletion),
            EUnitLiteral(UnitSymbol.of("MJ_net_calorific_value"), 1.0, resourceUseFossils),
            EUnitLiteral(UnitSymbol.of("kg_Sb_Eq"), 1.0, resourceUseMineralsAndMetals),
            EUnitLiteral(UnitSymbol.of("m3_world_eq_deprived"), 1.0, waterUse),
        ).associateBy { it.symbol.toString() }

        // composite dimensions
        val area = length.multiply(length)
        val volume = length.multiply(area)
        val land_occupation = area.multiply(time)
        val transport = mass.multiply(length)
        val power = energy.divide(time)
        val none = Dimension.None
        val length_time = length.multiply(time)
        val person_distance = none.multiply(length)
        val mass_time = mass.multiply(time)
        val volume_time = volume.multiply(time)
        val illuminance = luminous_intensity.divide(area)

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

        fun primitiveDimensionMap(): Map<String, Dimension> =
            primitiveUnits<Any>().mapValues { it.value.dimension }

        fun <Q> units(): Register<DataExpression<Q>> = Register.from(unitMap())
        fun dimensions(): Register<Dimension> = Register.from(primitiveDimensionMap())

    }
}
