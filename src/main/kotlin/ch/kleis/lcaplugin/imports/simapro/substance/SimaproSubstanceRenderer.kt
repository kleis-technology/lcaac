package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.simapro.sanitizeUnit
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.refdata.ElementaryFlowBlock
import org.openlca.simapro.csv.refdata.ElementaryFlowRow
import java.io.File


class SimaproSubstanceRenderer {
    var nbSubstances = 0

    companion object {
        fun resolveSimaproType(type: ElementaryFlowType, name: String): SubstanceType {
            return when (type) {
                ElementaryFlowType.RESOURCES ->
                    return when {
                        name.startsWith("Occupation,") || name.startsWith("Transformation,") -> SubstanceType.LAND_USE
                        else -> SubstanceType.RESOURCE
                    }

                else -> SubstanceType.EMISSION
            }
        }
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
        compartiment: String,
        writer: ModelWriter
    ) {
        val uid = ModelWriter.sanitizeAndCompact(element.name())
        val description = element.comment()
            ?.split("\n")
            ?.map { ModelWriter.compactText(it) }
            ?.filter { it.isNotBlank() }
            ?: listOf("")
        val optionalPlatform =
            if (element.platformId().isNullOrBlank()) "" else "\"platformId\" = \"${element.platformId()}\""
        val subType = resolveSimaproType(type, element.name())
        writer.write(
            "substances${File.separatorChar}$compartiment${File.separatorChar}${uid}.lca",
            """
substance $uid {

    name = "${element.name()}"
    type = $subType
    compartment = "$compartiment"
    reference_unit = ${sanitizeUnit(element.unit())}

    impacts {
        1 ${sanitizeUnit(element.unit())} $uid
    }

    meta {
        "generator" = "kleis-lca-generator"
        "description" = "${ModelWriter.padButFirst(description, 12)}"
        "casNumber" = "${element.cas()}"
        $optionalPlatform
    }
}"""
        )
        nbSubstances++
    }

}
