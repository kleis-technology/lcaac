package ch.kleis.lcaplugin.actions.csv

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStream

class CsvRequestReader(
    private val processName: String,
    private val matchLabels: Map<String, String>,
    private val inputStream: InputStream,
) {
    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .build()

    fun read(): List<CsvRequest> {
        val parser = CSVParser(inputStream.reader(), format)
        val header = parser.headerMap
        return parser.records.map { record ->
            CsvRequest(
                processName,
                matchLabels,
                header,
                record.toList()
            )
        }
    }
}
