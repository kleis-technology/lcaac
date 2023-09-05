package ch.kleis.lcaplugin.imports.shared.serializer

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.ModelWriter.Companion.padButFirst
import ch.kleis.lcaplugin.imports.model.*

object ProcessSerializer {
    fun serialize(e: ImportedExchange): List<CharSequence> {
        val comments = e.comments
            .flatMap(CharSequence::lines)
            .filter { it.isNotBlank() }
            .map { "// $it" }
        val printCommented = if (e.printAsComment) "// " else ""
        val txt = when (e) {
            is ImportedProductExchange -> "${printCommented}${e.qty} ${e.unit} ${e.uid} allocate ${e.allocation} percent"
            is ImportedImpactExchange -> "${printCommented}${e.qty} ${e.unit} ${e.uid}"

            is ImportedInputExchange -> {
                val fromProcess = e.fromProcess?.let { " from $it" } ?: ""
                "${printCommented}${e.qty} ${e.unit} ${e.uid}${fromProcess}"
            }

            is ImportedBioExchange -> {
                val sub = e.subCompartment?.let { ", sub_compartment = \"$it\"" } ?: ""
                """${printCommented}${e.qty} ${e.unit} ${e.uid}(compartment = "${e.compartment}"$sub)"""
            }
        }
        return comments + txt
    }

    fun serialize(block: Sequence<ImportedExchange>, pad: Int): CharSequence {
        val prefix = " ".repeat(pad)
        return block.flatMap { serialize(it) }
            .joinTo(StringBuilder(), "\n$prefix", prefix)
    }

    fun serialize(p: ImportedProcess): CharSequence {

        val builder = StringBuilder()

        // Meta
        val metaBloc = ModelWriter.blockKeyValue(p.meta.entries, 8)
        builder.append(
            """

process ${p.uid} {

    meta {
$metaBloc
    }
"""
        )
        // Params
        if (p.params.isNotEmpty()) {
            val paramsBloc = p.params.map { "${it.symbol} = ${it.value}" }
            builder.append(
                """

    params {
        ${padButFirst(paramsBloc, 8)}
    }
"""
            )
        }

        val blocks = listOf(
            "products" to p.productBlocks,
            "inputs" to p.inputBlocks,
            "emissions" to p.emissionBlocks,
            "resources" to p.resourceBlocks,
            "land_use" to p.landUseBlocks,
            "impacts" to p.impactBlocks,
        )

        blocks.forEach { (keyword, blockList) ->
            blockList.forEach { block ->
                val doc = if (block.comment?.isNotBlank() == true) " // ${block.comment}" else ""
                builder.append(
                    """
    $keyword {$doc
${serialize(block.exchanges, 8)}
    }
"""
                )
            }
        }

        builder.append("}")
        return builder
    }
}