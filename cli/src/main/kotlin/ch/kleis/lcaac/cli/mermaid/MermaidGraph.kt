package ch.kleis.lcaac.cli.mermaid

import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.value.BioExchangeValue
import ch.kleis.lcaac.core.lang.value.ImpactValue
import ch.kleis.lcaac.core.lang.value.ProcessValue
import ch.kleis.lcaac.core.lang.value.TechnoExchangeValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

enum class MermaidGraphOption {
    SHOW_PRODUCTS,
    SHOW_QUANTITIES,
}

class MermaidGraph(
    private val trace: EvaluationTrace<BasicNumber>,
    private val options: Set<MermaidGraphOption> = emptySet(),
) {
    fun render(): String {
        val system = trace.getSystemValue()
        val processes = system.processes
        val substanceCharacterizations = system.substanceCharacterizations

        // Nodes
        val processIdMap = processes.mapIndexed { i, p -> p to "p$i" }.toMap()
        val danglingProducts = processes.flatMap { it.inputs }
            .map { it.product }
            .filter { system.productToProcessMap[it] == null }
            .toSet()
        val danglingProductIdMap = danglingProducts.mapIndexed { i, prod -> prod to "prod$i" }.toMap()
        val danglingSubstances = processes.flatMap { it.biosphere }
            .map { it.substance }
            .filter { system.substanceToSubstanceCharacterizationMap[it] == null }
            .toSet()
        val danglingSubstanceIdMap = danglingSubstances.mapIndexed { i, sub -> sub to "sub$i" }.toMap()
        val indicators = (
            processes.flatMap { it.impacts } +
            substanceCharacterizations.flatMap { it.impacts }
        ).map { it.indicator }.toSet()
        val indicatorIdMap = indicators.mapIndexed { i, ind -> ind to "ind$i" }.toMap()

        return buildString {
            appendLine("flowchart BT")
            processes.forEach { process ->
                appendLine("    ${processIdMap[process]}[\"${nodeLabel(process)}\"]")
            }
            danglingProductIdMap.forEach { (product, id) ->
                appendLine("    $id[\"${product.getDisplayName()}\"]")
            }
            danglingSubstanceIdMap.forEach { (substance, id) ->
                appendLine("    $id[\"${substance.getDisplayName()}\"]")
            }
            indicatorIdMap.forEach { (indicator, id) ->
                appendLine("    $id[\"${indicator.name}\"]")
            }

            // Edges from process inputs
            processes.forEach { consumer ->
                consumer.inputs.forEach { input ->
                    val producer = system.productToProcessMap[input.product]
                    val sourceId = producer?.let { processIdMap[it] } ?: danglingProductIdMap[input.product]
                    if (sourceId != null) {
                        val label = edgeLabel(input)
                        if (label != null) {
                            appendLine("    $sourceId -->|\"$label\"| ${processIdMap[consumer]}")
                        } else {
                            appendLine("    $sourceId --> ${processIdMap[consumer]}")
                        }
                    }
                }
            }

            // Edges from process biosphere
            processes.forEach { consumer ->
                consumer.biosphere.forEach { emission ->
                    val sc = system.substanceToSubstanceCharacterizationMap[emission.substance]
                    if (sc != null) {
                        sc.impacts.forEach { impact ->
                            val label = edgeLabel(impact)
                            if (label != null) {
                                appendLine("    ${indicatorIdMap[impact.indicator]} -->|\"$label\"| ${processIdMap[consumer]}")
                            } else {
                                appendLine("    ${indicatorIdMap[impact.indicator]} --> ${processIdMap[consumer]}")
                            }
                        }
                    } else {
                        val label = edgeLabel(emission)
                        if (label != null) {
                            appendLine("    ${danglingSubstanceIdMap[emission.substance]} -->|\"$label\"| ${processIdMap[consumer]}")
                        } else {
                            appendLine("    ${danglingSubstanceIdMap[emission.substance]} --> ${processIdMap[consumer]}")
                        }
                    }
                }
            }

            // Edges from process direct impacts
            processes.forEach { consumer ->
                consumer.impacts.forEach { impact ->
                    val label = edgeLabel(impact)
                    if (label != null) {
                        appendLine("    ${indicatorIdMap[impact.indicator]} -->|\"$label\"| ${processIdMap[consumer]}")
                    } else {
                        appendLine("    ${indicatorIdMap[impact.indicator]} --> ${processIdMap[consumer]}")
                    }
                }
            }
        }
    }

    private fun edgeLabel(input: TechnoExchangeValue<BasicNumber>): String? {
        val parts = mutableListOf<String>()
        if (MermaidGraphOption.SHOW_QUANTITIES in options) parts.add(input.quantity.toString())
        if (MermaidGraphOption.SHOW_PRODUCTS in options) parts.add(input.product.name)
        return if (parts.isEmpty()) null else parts.joinToString(" ")
    }

    private fun edgeLabel(emission: BioExchangeValue<BasicNumber>): String? {
        val parts = mutableListOf<String>()
        if (MermaidGraphOption.SHOW_QUANTITIES in options) parts.add(emission.quantity.toString())
        if (MermaidGraphOption.SHOW_PRODUCTS in options) parts.add(emission.substance.getDisplayName())
        return if (parts.isEmpty()) null else parts.joinToString(" ")
    }

    private fun edgeLabel(impact: ImpactValue<BasicNumber>): String? {
        val parts = mutableListOf<String>()
        if (MermaidGraphOption.SHOW_QUANTITIES in options) parts.add(impact.quantity.toString())
        if (MermaidGraphOption.SHOW_PRODUCTS in options) parts.add(impact.indicator.name)
        return if (parts.isEmpty()) null else parts.joinToString(" ")
    }

    private fun nodeLabel(process: ProcessValue<BasicNumber>): String {
        if (process.labels.isEmpty()) return process.name
        val labelsStr = process.labels.entries
            .sortedBy { it.key }
            .joinToString(", ") { "${it.key}: ${it.value.s}" }
        return "${process.name}\\n{$labelsStr}"
    }
}
