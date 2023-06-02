package ch.kleis.lcaplugin.imports.ecospold.lcai

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.ecospold.lcai.EcospoldImporter.ProcessDictRecord
import ch.kleis.lcaplugin.imports.shared.ProcessSerializer
import com.intellij.openapi.diagnostic.Logger
import spold2.DataSet
import java.io.File

const val LAND_OR_RESOURCE_COMPARTMENT = "natural resource"
const val LAND_SUBCOMPARTMENT = "land"

class ProcessRenderer {
    companion object {
        private val LOG = Logger.getInstance(ProcessRenderer::class.java)
    }

    var nbProcesses: Int = 0
        private set
    var processDict: Map<String, ProcessDictRecord>? = null

    fun render(data: DataSet, w: ModelWriter, processComment: String?) {
        nbProcesses++

        val category = category(data)

        val subFolder = if (category == null) "" else "${category}${File.separatorChar}"
        val process = EcoSpold2ProcessMapper.map(data)
        val str = ProcessSerializer.serialize(process)

        w.write(
            "processes${File.separatorChar}$subFolder${process.uid}.lca",
            str, index = true, closeAfterWrite = true
        )
    }

    private fun category(data: DataSet): String? {
        val desc = data.description?.classifications
            ?.firstOrNull { it.system == "EcoSpold01Categories" }
            ?.value
        return desc?.let { ModelWriter.sanitizeAndCompact(it) }
    }


    enum class TechnoType(val value: Int, val text: String) : Comparable<TechnoType> {
        PRODUCT(0, "products"), INPUT(1, "inputs")
    }

    enum class BiosphereType(val value: Int, val text: String) : Comparable<BiosphereType> {
        EMISSION(0, "emissions"), LANDUSE(1, "land_use"), RESOURCES(2, "resources")
    }

}