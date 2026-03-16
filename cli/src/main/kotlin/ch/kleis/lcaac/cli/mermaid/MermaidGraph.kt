package ch.kleis.lcaac.cli.mermaid

import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.value.ProcessValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

class MermaidGraph(private val trace: EvaluationTrace<BasicNumber>) {
    fun render(): String {
        val system = trace.getSystemValue()
        val processes = system.processes.sortedWith(compareBy({ it.name }, { it.labels.toString() }))
        val idMap = processes.mapIndexed { i, p -> p to "p$i" }.toMap()

        return buildString {
            appendLine("flowchart BT")
            processes.forEach { process ->
                appendLine("    ${idMap[process]}[\"${nodeLabel(process)}\"]")
            }
            processes.forEach { consumer ->
                consumer.inputs.forEach { input ->
                    val producer = system.productToProcessMap[input.product]
                    if (producer != null) {
                        appendLine("    ${idMap[producer]} --> ${idMap[consumer]}")
                    }
                }
            }
        }
    }

    private fun nodeLabel(process: ProcessValue<BasicNumber>): String {
        if (process.labels.isEmpty()) return process.name
        val labelsStr = process.labels.entries
            .sortedBy { it.key }
            .joinToString(", ") { "${it.key}: ${it.value.s}" }
        return "${process.name}\\n{$labelsStr}"
    }
}
