package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.Renderer
import org.openlca.simapro.csv.refdata.ElementaryFlowBlock
import org.openlca.simapro.csv.refdata.ElementaryFlowRow

class SubstanceRenderer : Renderer<ElementaryFlowBlock> {
    override fun render(block: ElementaryFlowBlock, writer: ModelWriter) {
        val compartimentRaw = block.type().compartment().lowercase()
        val compartiment = ModelWriter.sanitizeAndCompact(compartimentRaw)
        block.flows().forEach { render(it, compartiment, writer) }
    }

    private fun render(element: ElementaryFlowRow, compartiment: String, writer: ModelWriter) {
        val uid = ModelWriter.sanitizeAndCompact("${element.name()}-$compartiment")
        val description = element.comment()
            ?.split("\n")
            ?.map { ModelWriter.compactText(it) }
            ?.filter { it.isNotBlank() }
            ?: listOf("")
        val optionalPlatform =
            if (element.platformId().isNullOrBlank()) "" else "platformId = \"${element.platformId()}\""
        writer.write(
            "substances/$compartiment",
            """
substance $uid {

    name = "${element.name()}"
    compartment = "$compartiment"
    reference_unit = ${element.unit()}

    impacts {
        1 ${element.unit()} $uid
    }

    meta {
        type = "emissions"
        generator = "kleis-lca-generator"
        description = "${ModelWriter.padButFirst(description, 12)}"
        casNumber = "${element.cas()}"
        $optionalPlatform
    }
}"""
        )
    }

}