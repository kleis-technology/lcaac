package ch.kleis.lcaac.cli.csv

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

    fun iterator(): Iterator<CsvRequest> {
        return object: Iterator<CsvRequest> {
            private val parser = CSVParser(inputStream.reader(), format)
            private val header = parser.headerMap
            private val iterator = parser.iterator()

            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): CsvRequest {
                val record = iterator.next()
                return CsvRequest(
                    processName,
                    matchLabels,
                    header,
                    record.toList()
                )
            }
        }
    }
}
