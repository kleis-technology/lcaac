package ch.kleis.lcaac.cli.csv.assess

import ch.kleis.lcaac.cli.cmd.OutputFormat
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.prelude.Prelude
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

class AssessCsvResultWriter(
    private val format: OutputFormat = OutputFormat.TEXT,
    private val indicators: Set<String> = emptySet(),
) {
    private val csvFormat = when (format) {
        OutputFormat.CSV -> CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setRecordSeparator(System.lineSeparator())
            .build()

        else -> null
    }

    // JSON state
    private var isFirstRow = true

    // TEXT state
    private var textHeaders: List<String> = emptyList()
    private val textRows: MutableList<List<String>> = mutableListOf()

    fun header(first: AssessCsvResult): String {
        val filtered = filteredImpacts(first.impacts)
        val headers = first.request.columns()
            .plus(listOf("product", "amount", "reference unit"))
            .plus(filtered.toList().flatMap {
                listOf(it.first.getShortName(), "${it.first.getShortName()}_unit")
            })
        return when (format) {
            OutputFormat.JSON -> "[\n"
            OutputFormat.TEXT -> {
                textHeaders = headers
                ""
            }
            OutputFormat.CSV -> {
                val s = StringBuilder()
                CSVPrinter(s, csvFormat!!).printRecord(headers)
                s.toString()
            }
        }
    }

    fun row(result: AssessCsvResult): String {
        val filtered = filteredImpacts(result.impacts)
        val cells = result.request.arguments()
            .plus(listOf(result.output.getShortName(), "1.0", result.output.referenceUnit().symbol.toString()))
            .plus(filtered.toList().flatMap {
                listOf(
                    it.second.amount.toString(),
                    Prelude.sanitize(it.second.unit.toString(), toLowerCase = false),
                )
            })
        return when (format) {
            OutputFormat.JSON -> {
                val sb = StringBuilder()
                if (!isFirstRow) sb.append(",\n")
                isFirstRow = false
                sb.append(toJson(result, filtered))
                sb.toString()
            }
            OutputFormat.TEXT -> {
                textRows.add(cells)
                ""
            }
            OutputFormat.CSV -> {
                val s = StringBuilder()
                CSVPrinter(s, csvFormat!!).printRecord(cells)
                s.toString()
            }
        }
    }

    fun footer(): String = when (format) {
        OutputFormat.JSON -> "\n]\n"
        OutputFormat.TEXT -> formatAsTable(textHeaders, textRows)
        OutputFormat.CSV -> ""
    }

    private fun filteredImpacts(
        impacts: Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>>,
    ) = if (indicators.isEmpty()) impacts
    else impacts.filter { indicators.contains(it.key.getShortName()) }

    private fun toJson(
        result: AssessCsvResult,
        impacts: Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>>,
    ): String {
        val requestJson = result.request.toMap().entries
            .joinToString(", ") { "\"${it.key.esc()}\": \"${it.value.esc()}\"" }
        val impactsJson = impacts.entries
            .joinToString(", ") {
                "\"${it.key.getShortName().esc()}\": {\"amount\": ${it.value.amount}, \"unit\": \"${it.value.unit.toString().esc()}\"}"
            }
        return buildString {
            appendLine("{")
            appendLine("  \"request\": {$requestJson},")
            appendLine("  \"product\": \"${result.output.getShortName().esc()}\",")
            appendLine("  \"amount\": 1.0,")
            appendLine("  \"reference_unit\": \"${result.output.referenceUnit().symbol.toString().esc()}\",")
            appendLine("  \"impacts\": {$impactsJson}")
            append("}")
        }
    }
}

private fun formatAsTable(headers: List<String>, rows: List<List<String>>): String {
    if (headers.isEmpty()) return ""
    val allRows = listOf(headers) + rows
    val colWidths = headers.indices.map { col -> allRows.maxOf { row -> row.getOrElse(col) { "" }.length } }
    return buildString {
        appendLine(headers.mapIndexed { i, h -> h.padEnd(colWidths[i]) }.joinToString("  ").trimEnd())
        appendLine(colWidths.joinToString("  ") { "-".repeat(it) })
        rows.forEach { row ->
            appendLine(row.mapIndexed { i, cell -> cell.padEnd(colWidths[i]) }.joinToString("  ").trimEnd())
        }
    }
}

private fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
