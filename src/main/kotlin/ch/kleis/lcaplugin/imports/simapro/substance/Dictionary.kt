package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.MissingLibraryFileException
import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.nio.charset.Charset
import kotlin.streams.asSequence


interface Dictionary {
    fun realKeyForSubstance(
        name: String,
        type: String,
        unit: String,
        compartment: String,
        sub: String? = null
    ): SubstanceKey
}

class SimaproDictionary : Dictionary {
    override fun realKeyForSubstance(
        name: String,
        type: String,
        unit: String,
        compartment: String,
        sub: String?
    ): SubstanceKey {
        // Simapro Substance don't know subCompartment
        return SubstanceKey(name, type, compartment, "")
    }
}

enum class LandUseType(val value: String) { OCCUPATION("Land occupation"), TRANSFORMATION("Land transformation") }

const val NON_RENEWABLE = "non-renewable"
const val RENEWABLE = "renewable"

class Ef3xDictionary(private val dict: Map<SubstanceKey, SubstanceKey>) : Dictionary {
    companion object {
        val LOG = Logger.getInstance(Ef3xDictionary::class.java)

        @Throws(MissingLibraryFileException::class)
        fun fromClassPath(
            pathFilter: String,
            dictFileName: String = "META-INF/dictionary.csv"
        ): Dictionary {
            val csvFormat = CSVFormat.Builder.create().setDelimiter(";").setHeader().build()
            val url = Companion::class.java.classLoader.getResources(dictFileName).asSequence()
                .find { it.path.contains(pathFilter) }
                ?: throw MissingLibraryFileException("Unable to load dictionary $dictFileName in library $pathFilter")
            val dict = CSVParser.parse(
                url.openStream(), Charset.forName("UTF-8"), csvFormat
            ).stream().asSequence().associate { SubstanceKey(it) to SubstanceKey(it) }
            return Ef3xDictionary(dict)
        }
    }

    private val subCompartmentMapping = mapOf(
        "low. pop." to EfCategories.SubCompartiment.NON_URBAN_HIGH_STACK.value, // 771
        "high. pop." to EfCategories.SubCompartiment.URBAN_AIR_CLOSE_TO_GROUND.value, // 540
        "stratosphere + troposphere" to EfCategories.SubCompartiment.LOWER_STRATOSPHERE_AND_UPPER_TROPOSPHERE.value, // 519
        "low. pop., long-term" to EfCategories.SubCompartiment.LONG_TERM.value, //
//                groundwater
//                biotic
//                industrial
//                agricultural
//                ocean
//                land
//                river
//                forestry
//                low. pop.
//                groundwater, long-term
    )

    private val resourceCompartmentMapping = mapOf(
        "in ground" to EfCategories.Compartiment.GROUND.value,
        "in water" to EfCategories.Compartiment.WATER.value,
        "in air" to EfCategories.Compartiment.AIR.value
    )

    private val landUseSubCompMapping = mapOf(
        "annual crop" to "arable",
        "arable land" to "arable",
        "grassland, natural" to "grassland",
        "pasture, man made" to "pasture/meadow",
        "pasture, man made, extensive" to "pasture/meadow, extensive",
        "pasture, man made, intensive" to "pasture/meadow, intensive",
        "permanent crop" to "permanent crops",
//                sea and ocean
//                seabed, drilling and mining
//                seabed, infrastructure
        "shrub land, sclerophyllous" to "shrub land",
        "urban/industrial fallow (non-use)" to "urban/industrial fallow",
        "water bodies, artificial" to "water bodies",
        "river" to "rivers",
        "wetland" to "wetlands",
        ", unspecified use" to "",
        ", unspecified" to "",
        "waterbody" to "water bodies",
        "(non-use)" to "", // TODO Check : Good Idea or Bug ?
    )
    private val landUseSubCompCache = HashMap<String, SubstanceKey>()

    override fun realKeyForSubstance(
        name: String,
        type: String,
        unit: String,
        compartment: String,
        sub: String?
    ): SubstanceKey {
        return when (type) {
            SubstanceType.LAND_USE.value -> {
                keyForLandUse(name, type)
            }

            SubstanceType.RESOURCE.value -> {
                val realComp = resourceCompartmentMapping[sub] ?: sub ?: "null_sub_comp"
                val key = SubstanceKey(name, type, realComp, null)
                return tryKeyAndVariation(key) ?: tryKeyAndVariation(key.removeFromName(unit)) ?: key
            }

            SubstanceType.EMISSION.value -> {
                val realSubComp = subCompartmentMapping[sub] ?: sub
                val key = SubstanceKey(name, type, compartment, realSubComp)
                return tryKeyAndVariation(key) ?: tryKeyAndVariation(key.removeFromName(unit)) ?: key
            }

            else -> SubstanceKey(name, type, compartment, sub)
        }
    }

    private fun keyForLandUse(name: String, type: String): SubstanceKey {
        val existing = landUseSubCompCache[name]
        return if (existing != null) {
            existing
        } else {
            val (newName, realComp) = when {
                name.startsWith("Occupation, ") ->
                    name.substring("Occupation, ".length, name.length) to LandUseType.OCCUPATION.value

                name.startsWith("Transformation, ") ->
                    name.substring("Transformation, ".length, name.length) to LandUseType.TRANSFORMATION.value

                else -> {
                    LOG.warn("Unknown LandUseType for name = $name")
                    name to "Unknown"
                }
            }
            var realName = newName
            if (realComp != "Unknown") {
                landUseSubCompMapping.entries.forEach { (from, to) -> realName = realName.replace(from, to) }
            }
            val result = SubstanceKey(realName, type, realComp, null, hasChanged = newName != realName)
            landUseSubCompCache[name] = result
            result
        }
    }

    private fun tryKeyAndVariation(key: SubstanceKey) =
        get(key) ?: get(key.withoutSub()) ?: get(key.sub(RENEWABLE)) ?: get(key.sub(NON_RENEWABLE))

    /* When the key exists in the dictionary, we have to return the one in parameter and not the one in the dict, because the equivalent
     * one in the dictionary differs a bit: they probably not have the same value for the field key.hasChanged
     * that is not part of equals/hashCode.
     */
    private fun get(key: SubstanceKey): SubstanceKey? {
        return if (dict[key] == null) null else key
    }
}

