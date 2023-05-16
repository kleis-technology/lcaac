package ch.kleis.lcaplugin.actions.csv

import org.apache.commons.csv.CSVRecord

class CsvRequest(
    val processName: String,
    private val header: Map<String, Int>,
    private val record: CSVRecord,
) {
    fun columns(): List<String> {
        return header.toList().map { it.first }
    }

    fun arguments(): List<String> {
        return header.toList().map { record[it.second] }
    }

    operator fun get(name: String): String {
        return header[name]
            ?.let { record[it] }
            ?: throw NoSuchElementException(name)
    }
}
