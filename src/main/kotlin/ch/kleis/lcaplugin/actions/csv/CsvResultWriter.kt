package ch.kleis.lcaplugin.actions.csv

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer

class CsvResultWriter(
    outputStream: OutputStream,
    private val writer: Writer = OutputStreamWriter(outputStream)
) {
    fun write(results: List<CsvResult>) {
        val first = results.firstOrNull() ?: return
        val header = first.request.columns()
            .plus(listOf("product", "reference unit"))
            .plus(first.impacts.toList().map { "${it.first.getShortName()} [${it.first.referenceUnit().symbol}]" })
            .joinToString()
        writer.appendLine(header)

        results.forEach { result ->
            val line = result.request.arguments()
                .plus(listOf(result.output.getShortName(), result.output.referenceUnit().symbol))
                .plus(result.impacts.toList().map { it.second.amount.toString() })
                .joinToString()
            writer.appendLine(line)
        }
    }

    fun flush() {
        writer.flush()
    }
}
