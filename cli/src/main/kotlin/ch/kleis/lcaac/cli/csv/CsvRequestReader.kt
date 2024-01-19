package ch.kleis.lcaac.cli.csv

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStream

class CsvRequestReader(
        private val processName: String,
        private val matchLabels: Map<String, String>,
        private val inputStream: InputStream,
        private val overrideArguments: Map<String, String> = emptyMap(),
) {
    private val format = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build()

    fun iterator(): Iterator<CsvRequest> {
        return object : Iterator<CsvRequest> {
            private val parser = CSVParser(inputStream.reader(), format)
            private val header = parser.headerMap
            private val augmentedHeader = header.plus(
                    overrideArguments.keys
                            .filter { !header.containsKey(it) }
                            .mapIndexed { index, k -> k to (header.size + index) }
            )
            private val iterator = parser.iterator()

            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): CsvRequest {
                val record = iterator.next().toMap()
                        .plus(overrideArguments)
                        .toList()
                        .sortedBy { augmentedHeader[it.first] }
                        .map { it.second }
                return CsvRequest(
                        processName,
                        matchLabels,
                        augmentedHeader,
                        record,
                )
            }
        }
    }
}
