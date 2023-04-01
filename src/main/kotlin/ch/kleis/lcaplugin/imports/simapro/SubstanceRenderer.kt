package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.Renderer
import org.openlca.simapro.csv.refdata.ElementaryFlowBlock
import org.openlca.simapro.csv.refdata.ElementaryFlowRow

class SubstanceRenderer : Renderer<ElementaryFlowBlock> {
    override fun render(block: ElementaryFlowBlock, writer: ModelWriter) {
        val compartimentRaw = block.type().compartment().lowercase()
        val compartiment = ModelWriter.sanitizeString(compartimentRaw)
//            if (block.type() == ElementaryFlowType.EMISSIONS_TO_AIR) {
//            "air"
//        } else if (block.type() == ElementaryFlowType.EMISSIONS_TO_SOIL) {
//            "soil"
//        } else if (block.type() == ElementaryFlowType.EMISSIONS_TO_WATER) {
//            "soil"
//        } else if (block.type() == ElementaryFlowType.FINAL_WASTE_FLOWS) {
//            "waste"
//        } else if (block.type() == ElementaryFlowType.ECONOMIC_ISSUES) {
//            "eco"
//        } else if (block.type() == ElementaryFlowType.NON_MATERIAL_EMISSIONS) {
//            "non_mat"
//        } else if (block.type() == ElementaryFlowType.RESOURCES) {
//            "res"
//        } else if (block.type() == ElementaryFlowType.SOCIAL_ISSUES) {
//            "social"
//        } else {
//            "unknown"
//        }
        block.flows().forEach { render(it, compartiment, writer) }
    }

    private fun render(element: ElementaryFlowRow, compartiment: String, writer: ModelWriter) {
        val uid = ModelWriter.sanitizeString("${element.name()}-$compartiment")
        val description = ModelWriter.compactText(element.comment())
        writer.write(
            "substances/$compartiment.lca",
            """
substance $uid {

    name = "${element.name()}-$compartiment"
    compartment = "$compartiment"
    sub_compartment = ""
    reference_unit = ${element.unit()}

    impacts {
        1 ${element.unit()} $uid
    }

    meta {
        type = "emissions"
        generator = "kleis-lca-generator"
        description = "$description"
        casNumber = "${element.cas()}"
        platformId = "${element.platformId()}"
    }
}"""
        )
    }

}