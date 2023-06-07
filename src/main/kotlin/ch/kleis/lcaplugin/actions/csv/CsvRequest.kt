package ch.kleis.lcaplugin.actions.csv

class CsvRequest(
    val processName: String,
    val matchLabels: Map<String, String>,
    private val header: Map<String, Int>,
    private val record: List<String>,
) {
    fun columns(): List<String> {
        return header.toList().map { it.first }
    }

    fun arguments(): List<String> {
        return header.toList().map { record[it.second] }
    }

    operator fun get(name: String): String? {
        return header[name]
            ?.let { record[it] }
    }
}
