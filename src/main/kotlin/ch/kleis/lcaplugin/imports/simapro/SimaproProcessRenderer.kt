package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.ide.imports.simapro.SubstanceImportMode
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.shared.ProcessSerializer
import org.openlca.simapro.csv.process.ProcessBlock
import java.io.File


class ProcessRenderer(mode: SubstanceImportMode) {
    private val mapper = SimaproProcessMapper.of(mode)
    var nbProcesses: Int = 0


    fun render(processBlock: ProcessBlock, writer: ModelWriter) {
        val subFolder = if (processBlock.category() == null) "" else "${processBlock.category()}${File.separatorChar}"
        val process = mapper.map(processBlock)
        val str = ProcessSerializer.serialize(process)

        writer.write(
            "processes${File.separatorChar}$subFolder${process.uid}.lca",
            str, index = true, closeAfterWrite = true
        )
        nbProcesses++
    }

}
