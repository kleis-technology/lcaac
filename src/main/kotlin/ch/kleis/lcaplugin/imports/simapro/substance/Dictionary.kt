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
        subCompartment: String? = null
    ): SubstanceKey
}

class SimaproDictionary : Dictionary {
    override fun realKeyForSubstance(
        name: String,
        type: String,
        unit: String,
        compartment: String,
        subCompartment: String?
    ): SubstanceKey {
        // Simapro Substance do not deal with type...
        return SubstanceKey(name, type, compartment, "")
    }
}

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
                .filter { it.path.contains(pathFilter) }
                .firstOrNull()
                ?: throw MissingLibraryFileException("Unable to load dictionary $dictFileName in library $pathFilter")
            val dict = CSVParser.parse(
                url.openStream(), Charset.forName("UTF-8"), csvFormat
            ).stream()
                .asSequence()
                .map { SubstanceKey(it) }
                .map { it to it }
                .toMap()
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


    override fun realKeyForSubstance(
        name: String,
        type: String,
        unit: String,
        compartment: String,
        subCompartment: String?
    ): SubstanceKey {
        val realComp: String
        val realSubComp: String?
        if (type == SubstanceType.RESOURCE.value) {
            realComp = resourceCompartmentMapping[subCompartment] ?: subCompartment ?: "null_sub_comp"
            realSubComp = null
        } else {
            realComp = compartment
            realSubComp = subCompartmentMapping[subCompartment] ?: subCompartment
        }
        val key = SubstanceKey(name, type, realComp, realSubComp)
        return tryKeyAndVariation(key) ?: tryKeyAndVariation(key.removeFromName(unit)) ?: key
    }

    private fun tryKeyAndVariation(key: SubstanceKey) =
        get(key) ?: get(key.withoutSub()) ?: get(key.sub(RENEWABLE)) ?: get(key.sub(NON_RENEWABLE))

    /* Need to return param, because we need to have key.hasChanged, that may differ from the on in the dictionary */
    private fun get(key: SubstanceKey): SubstanceKey? {
        return if (dict[key] == null) null else key
    }
}

