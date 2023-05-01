package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.imports.ModelWriter
import org.openlca.simapro.csv.refdata.InputParameterBlock
import org.openlca.simapro.csv.refdata.InputParameterRow

class InputParameterRenderer {
    var nbParameters = 0
    fun render(block: InputParameterBlock, writer: ModelWriter) {
        if (block.parameters().size > 0) {
            val vars = block.parameters().flatMap { render(it) }
            writer.write(
                "main", """

                    variables {
                        ${ModelWriter.padButFirst(vars, 24)}
                    }

                    """.trimIndent(), false
            )
            nbParameters += block.parameters().size
        }
    }

    private fun render(param: InputParameterRow): List<String> {
        val comment = ModelWriter.asComment(param.comment())
        return comment.plus("${param.name()} = ${param.value()} u")
    }


}