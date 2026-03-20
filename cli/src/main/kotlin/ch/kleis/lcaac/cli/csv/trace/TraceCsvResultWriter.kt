package ch.kleis.lcaac.cli.csv.trace

import ch.kleis.lcaac.cli.cmd.OutputFormat
import ch.kleis.lcaac.core.lang.value.FullyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.PartiallyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations.toDouble
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

class TraceCsvResultWriter(
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
    private var isFirstResult = true

    // TEXT state
    private var textHeaders: List<String> = emptyList()
    private val textRows: MutableList<List<String>> = mutableListOf()

    fun header(first: TraceCsvResult): String {
        val filtered = filteredImpacts(first.trace.first().impacts)
        val headers = first.request.columns()
            .plus(listOf("depth", "d_amount", "d_unit", "d_product", "alloc", "name", "a", "b", "c", "amount", "unit"))
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

    @Suppress("DuplicatedCode")
    fun rows(result: TraceCsvResult): List<String> {
        return when (format) {
            OutputFormat.JSON -> {
                val sb = StringBuilder()
                if (!isFirstResult) sb.append(",\n")
                isFirstResult = false
                sb.append(resultToJson(result))
                listOf(sb.toString())
            }

            OutputFormat.TEXT -> {
                textRows.addAll(toRowCells(result))
                emptyList()
            }

            OutputFormat.CSV -> toRowCells(result).map { cells ->
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

    @Suppress("DuplicatedCode")
    private fun toRowCells(result: TraceCsvResult): List<List<String>> = result.trace.map { line ->
        val filtered = filteredImpacts(line.impacts)
        val demandedAmount = line.demandedProduct.quantity.amount
        val demandedUnit = line.demandedProduct.quantity.unit
        val demandedProductName = line.demandedProduct.product.name
        val allocationAmount = (line.demandedProduct.allocation?.amount?.toDouble()
            ?: 1.0) * (line.demandedProduct.allocation?.unit?.scale ?: 1.0)
        val supplyAmount = line.supply.amount.value * allocationAmount
        val prefix = result.request.arguments().plus(
            when (line.output) {
                is IndicatorValue<*> -> listOf(
                    line.depth.toString(),
                    demandedAmount.toString(),
                    demandedUnit.toString(),
                    demandedProductName,
                    allocationAmount.toString(),
                    line.output.name,
                    "", "", "",
                    supplyAmount.toString(),
                    line.supply.unit.toString(),
                )
                is ProductValue<*> -> listOf(
                    line.depth.toString(),
                    demandedAmount.toString(),
                    demandedUnit.toString(),
                    demandedProductName,
                    allocationAmount.toString(),
                    line.output.name,
                    line.output.fromProcessRef?.name ?: "",
                    line.output.fromProcessRef?.matchLabels?.toString() ?: "",
                    line.output.fromProcessRef?.arguments?.toString() ?: "",
                    supplyAmount.toString(),
                    line.supply.unit.toString(),
                )
                is FullyQualifiedSubstanceValue<*> -> listOf(
                    line.depth.toString(),
                    demandedAmount.toString(),
                    demandedUnit.toString(),
                    demandedProductName,
                    allocationAmount.toString(),
                    line.output.name,
                    line.output.compartment,
                    line.output.subcompartment ?: "",
                    line.output.type.toString(),
                    supplyAmount.toString(),
                    line.supply.unit.toString(),
                )
                is PartiallyQualifiedSubstanceValue<*> -> listOf(
                    line.depth.toString(),
                    demandedAmount.toString(),
                    demandedUnit.toString(),
                    demandedProductName,
                    allocationAmount.toString(),
                    line.output.name,
                    "", "", "",
                    supplyAmount.toString(),
                    line.supply.unit.toString(),
                )
                else -> listOf(
                    line.depth.toString(),
                    demandedAmount.toString(),
                    demandedUnit.toString(),
                    demandedProductName,
                    allocationAmount.toString(),
                    "", "", "", "",
                    supplyAmount.toString(),
                    line.supply.unit.toString(),
                )
            }
        )
        val impacts = filtered.flatMap {
            val impactAmount = it.value.amount.value * allocationAmount
            listOf(impactAmount.toString(), it.value.unit.toString())
        }
        prefix.plus(impacts).map { it.toString() }
    }

    @Suppress("DuplicatedCode")
    private fun resultToJson(result: TraceCsvResult): String {
        val requestJson = result.request.toMap().entries
            .joinToString(", ") { "\"${it.key.esc()}\": \"${it.value.esc()}\"" }
        val traceJson = result.trace.joinToString(",\n    ") { line ->
            val filtered = filteredImpacts(line.impacts)
            val demandedAmount = line.demandedProduct.quantity.amount
            val demandedUnit = line.demandedProduct.quantity.unit
            val demandedProductName = line.demandedProduct.product.name
            val allocationAmount = (line.demandedProduct.allocation?.amount?.toDouble()
                ?: 1.0) * (line.demandedProduct.allocation?.unit?.scale ?: 1.0)
            val supplyAmount = line.supply.amount.value * allocationAmount
            val (name, a, b, c) = when (line.output) {
                is IndicatorValue<*> -> listOf(line.output.name, "", "", "")
                is ProductValue<*> -> listOf(
                    line.output.name,
                    line.output.fromProcessRef?.name ?: "",
                    line.output.fromProcessRef?.matchLabels?.toString() ?: "",
                    line.output.fromProcessRef?.arguments?.toString() ?: "",
                )
                is FullyQualifiedSubstanceValue<*> -> listOf(
                    line.output.name,
                    line.output.compartment,
                    line.output.subcompartment ?: "",
                    line.output.type.toString(),
                )
                is PartiallyQualifiedSubstanceValue<*> -> listOf(line.output.name, "", "", "")
                else -> listOf("", "", "", "")
            }
            val impactsJson = filtered.entries.joinToString(", ") {
                val impactAmount = it.value.amount.value * allocationAmount
                "\"${it.key.getShortName().esc()}\": {\"amount\": $impactAmount, \"unit\": \"${it.value.unit.toString().esc()}\"}"
            }
            buildString {
                append("{")
                append("\"depth\": ${line.depth}, ")
                append("\"d_amount\": $demandedAmount, ")
                append("\"d_unit\": \"${demandedUnit.toString().esc()}\", ")
                append("\"d_product\": \"${demandedProductName.esc()}\", ")
                append("\"alloc\": $allocationAmount, ")
                append("\"name\": \"${name.esc()}\", ")
                append("\"a\": \"${a.esc()}\", ")
                append("\"b\": \"${b.esc()}\", ")
                append("\"c\": \"${c.esc()}\", ")
                append("\"amount\": $supplyAmount, ")
                append("\"unit\": \"${line.supply.unit.toString().esc()}\", ")
                append("\"impacts\": {$impactsJson}")
                append("}")
            }
        }
        return buildString {
            appendLine("{")
            appendLine("  \"request\": {$requestJson},")
            appendLine("  \"trace\": [")
            appendLine("    $traceJson")
            append("  ]")
            append("\n}")
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
