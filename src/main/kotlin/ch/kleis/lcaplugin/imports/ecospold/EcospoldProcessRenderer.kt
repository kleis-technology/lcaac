package ch.kleis.lcaplugin.imports.ecospold

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.ecospold.EcospoldImporter.ProcessDictRecord
import ch.kleis.lcaplugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaplugin.imports.shared.serializer.ProcessSerializer
import java.io.File

class EcospoldProcessRenderer {

    var nbProcesses: Int = 0
        private set

    fun render(
        data: ActivityDataset,
        w: ModelWriter,
        processDict: Map<String, ProcessDictRecord>,
        processComment: String,
        methodName: String
    ) {
        nbProcesses++

        val category = category(data)

        val subFolder = if (category == null) "" else "${category}${File.separatorChar}"
        val process = EcoSpoldProcessMapper.map(data, processDict, methodName)
        process.comments.add(processComment)
        val strProcess = ProcessSerializer.serialize(process)

        w.write(
            "processes${File.separatorChar}$subFolder${process.uid}",
            strProcess, index = false, closeAfterWrite = true
        )
    }

    private fun category(data: ActivityDataset): String? {
        val desc = data.description.classifications
            .firstOrNull { it.system == "EcoSpold01Categories" }
            ?.value
        return desc?.let { ModelWriter.sanitizeAndCompact(it) }
    }


}