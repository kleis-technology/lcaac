package ch.kleis.lcaplugin.imports.simapro.substance

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
        compartment: String,
        subCompartment: String? = null
    ): SubstanceKey
}

class SimaproDictionary : Dictionary {
    override fun realKeyForSubstance(
        name: String,
        type: String,
        compartment: String,
        subCompartment: String?
    ): SubstanceKey {
        // Simapro Substance do not deal with type...
        return SubstanceKey(name, "", compartment, subCompartment)
    }
}

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

    private val subCompartmentMapping = mapOf<String, String>(
        "low. pop." to EfCategories.SubCompartiment.NON_URBAN_HIGH_STACK.value
    ).withDefault { k -> k }


    override fun realKeyForSubstance(
        name: String,
        type: String,
        compartment: String,
        subCompartment: String?
    ): SubstanceKey {
        val realSubComp = subCompartmentMapping[subCompartment]

        val key = SubstanceKey(name, "", compartment, realSubComp)
        return dict[key] ?: dict[key.withoutSubCompartment()] ?: key
    }
}

