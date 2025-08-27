package ch.kleis.lcaac.cli.csv.assess

import ch.kleis.lcaac.core.prelude.Prelude
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

class AssessCsvResultWriter {
    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setRecordSeparator(System.lineSeparator())
        .build()

    fun header(first: AssessCsvResult): String {
        val header = first.request.columns()
            .plus(listOf("product", "amount", "reference unit"))
            .plus(first.impacts.toList()
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

    fun row(result: AssessCsvResult): String {
        val line = result.request.arguments()
            .plus(listOf(result.output.getShortName(), "1.0", result.output.referenceUnit().symbol))
            .plus(result.impacts.toList()
                .flatMap {
                    listOf(
                        it.second.amount.toString(),
                        Prelude.sanitize(it.second.unit.toString(), toLowerCase = false),
                    )
                })
        val s = StringBuilder()
        CSVPrinter(s, format).printRecord(line)
        return s.toString()
    }
}
