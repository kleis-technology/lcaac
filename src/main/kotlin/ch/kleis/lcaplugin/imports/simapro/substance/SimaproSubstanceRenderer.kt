package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.shared.SubstanceSerializer
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.refdata.ElementaryFlowBlock
import org.openlca.simapro.csv.refdata.ElementaryFlowRow
import java.io.File


class SimaproSubstanceRenderer {
    var nbSubstances = 0

    companion object {

    }

    fun render(block: ElementaryFlowBlock, writer: ModelWriter) {
        val compartimentRaw = block.type().compartment().lowercase()
        val compartiment = ModelWriter.sanitizeAndCompact(compartimentRaw)
        val type = block.type()
        block.flows().forEach { render(it, type, compartiment, writer) }
    }

    private fun render(
        element: ElementaryFlowRow,
        type: ElementaryFlowType,
        compartment: String,
        writer: ModelWriter
    ) {
        val uid = ModelWriter.sanitizeAndCompact(element.name())
        val substance = SimaproSubstanceMapper.map(element, type, compartment)
        val str = SubstanceSerializer.serialize(substance)
        writer.write(
            "substances${File.separatorChar}$compartment${File.separatorChar}${uid}.lca",
            str
        )
        nbSubstances++
    }


}
