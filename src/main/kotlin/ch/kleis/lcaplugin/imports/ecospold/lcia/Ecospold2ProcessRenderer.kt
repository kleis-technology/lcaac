package ch.kleis.lcaplugin.imports.ecospold.lcia

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.ecospold.lcia.EcospoldImporter.ProcessDictRecord
import ch.kleis.lcaplugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaplugin.imports.shared.serializer.ProcessSerializer
import ch.kleis.lcaplugin.imports.shared.serializer.SubstanceSerializer
import java.io.File

class Ecospold2ProcessRenderer {

    var nbProcesses: Int = 0
        private set
    var processDict: Map<String, ProcessDictRecord>? = null

    fun render(data: ActivityDataset, w: ModelWriter, processComment: String, methodName: String) {
        nbProcesses++

        val category = category(data)

        val subFolder = if (category == null) "" else "${category}${File.separatorChar}"
        val process = LciaProcessMapper(data).map()
        process.comments.add(processComment)
        val strProcess = ProcessSerializer.serialize(process)

        w.write(
            "processes${File.separatorChar}$subFolder${process.uid}.lca",
            strProcess, index = false, closeAfterWrite = false
        )
        val substance = EcoSpold2SubstanceMapper.map(data, methodName)
        val strSubstance = SubstanceSerializer.serialize(substance)
        w.write(
            "processes${File.separatorChar}$subFolder${process.uid}.lca",
            strSubstance, index = false, closeAfterWrite = true
        )


    }

    private fun category(data: ActivityDataset): String? {
        val desc = data.description.classifications
            .firstOrNull { it.system == "EcoSpold01Categories" }
            ?.value
        return desc?.let { ModelWriter.sanitizeAndCompact(it) }
    }


}