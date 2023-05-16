package ch.kleis.lcaplugin.actions.csv

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File

class CsvRequestReader(
    private val processName: String,
    private val file: File,
) {
    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .build()

    fun read(): List<CsvRequest> {
        val parser = CSVParser(file.inputStream().reader(), format)
        val header = parser.headerMap
        return parser.records.map { record ->
            CsvRequest(
                processName,
                header,
                record
            )
        }
    }
}
