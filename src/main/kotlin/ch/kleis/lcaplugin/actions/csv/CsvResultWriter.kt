package ch.kleis.lcaplugin.actions.csv

import java.io.File
import java.io.FileWriter

class CsvResultWriter(
    file: File
) {
    private val writer = FileWriter(file)

    fun write(results: List<CsvResult>) {
        val first = results.firstOrNull() ?: return
        val header = first.request.columns()
            .plus(listOf("product", "reference quantity"))
            .plus(first.impacts.toList().map { it.first.getShortName() })
            .joinToString()
        writer.appendLine(header)

        results.forEach { result ->
            val line = result.request.arguments()
                .plus(listOf(result.output.getShortName(), "1 ${result.output.referenceUnit().symbol}"))
                .plus(result.impacts.toList().map { it.second.toString() })
                .joinToString()
            writer.appendLine(line)
        }
    }

    fun flush() {
        writer.flush()
    }
}
