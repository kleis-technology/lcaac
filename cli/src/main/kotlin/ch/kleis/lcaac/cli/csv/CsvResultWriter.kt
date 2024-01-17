package ch.kleis.lcaac.cli.csv

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

class CsvResultWriter {
    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setRecordSeparator(System.lineSeparator())
        .build()

    fun header(first: CsvResult): String {
        val header = first.request.columns()
            .plus(listOf("product", "amount", "reference unit"))
            .plus(first.impacts.toList().map { "${it.first.getShortName()} [${it.first.referenceUnit().symbol}]" })
        val s = StringBuilder()
        CSVPrinter(s, format).printRecord(header)
        return s.toString()
    }

    fun row(result: CsvResult): String {
        val line = result.request.arguments()
            .plus(listOf(result.output.getShortName(), "1.0", result.output.referenceUnit().symbol))
            .plus(result.impacts.toList().map { it.second.amount.toString() })
        val s = StringBuilder()
        CSVPrinter(s, format).printRecord(line)
        return s.toString()
    }
}
