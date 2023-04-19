package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.Renderer
import org.openlca.simapro.csv.refdata.InputParameterBlock
import org.openlca.simapro.csv.refdata.InputParameterRow

class InputParameterRenderer : Renderer<InputParameterBlock> {
    override fun render(block: InputParameterBlock, writer: ModelWriter) {
        if (block.parameters().size > 0) {
            val vars = block.parameters().flatMap { render(it) }
            writer.write(
                "main", """

                    variables {
                        ${ModelWriter.padButFirst(vars, 24)}
                    }

                    """.trimIndent(), false
            )
        }
    }

    private fun render(param: InputParameterRow): List<String> {
        val comment = ModelWriter.asComment(param.comment())
        return comment.plus("${param.name()} = ${param.value()} u")
    }


}