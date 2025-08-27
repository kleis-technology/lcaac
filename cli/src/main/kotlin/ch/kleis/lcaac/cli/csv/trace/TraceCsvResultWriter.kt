package ch.kleis.lcaac.cli.csv.trace

import ch.kleis.lcaac.core.prelude.Prelude
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

class TraceCsvResultWriter {
    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setRecordSeparator(System.lineSeparator())
        .build()

    fun header(first: TraceCsvResult): String {
        val header = first.request.columns()
            .plus(listOf("depth", "product", "amount", "reference unit"))
            .plus(
                first.trace.first().impacts.toList()
                    .flatMap {
                        listOf(
                            it.first.getShortName(),
                            "${it.first.getShortName()}_unit",
                        )
                    })
        val s = StringBuilder()
        CSVPrinter(s, format).printRecord(header)
        return s.toString()
    }

    @Suppress("DuplicatedCode")
    fun rows(result: TraceCsvResult): List<String> {
        val lines = result.trace.map { item ->
            val line = result.request.arguments()
                .plus(listOf(item.depth.toString()))
                .plus(listOf(item.output.getShortName(), "1.0", item.output.referenceUnit().symbol))
                .plus(
                    item.impacts.toList()
                        .flatMap {
                            listOf(
                                it.second.amount.toString(),
                                Prelude.sanitize(it.second.unit.toString(), toLowerCase = false),
                            )
                        })
            val s = StringBuilder()
            CSVPrinter(s, format).printRecord(line)
            s.toString()
        }
        return lines
    }
}
