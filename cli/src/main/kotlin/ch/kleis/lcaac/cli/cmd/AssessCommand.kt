package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.cli.csv.CsvProcessor
import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.cli.csv.CsvRequestReader
import ch.kleis.lcaac.cli.csv.CsvResultWriter
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
class AssessCommand : CliktCommand(name = "assess", help = "Returns the unitary impacts of a process in CSV format") {
    val name: String by argument().help("Process name")
    val labels: Map<String, String> by option("-l", "--label")
            .help(
                """
                    Specify a process label as a key value pair.
                    Example: lcaac assess <process name> -l model="ABC" -l geo="FR".
                """.trimIndent())
            .associate()
    val path: File by option("-p", "--path").file(canBeFile = false).default(File(".")).help("Path to root folder.")
    val file: File? by option("-f", "--file").file(canBeDir = false)
            .help("""
                CSV file with parameter values.
                Example: `lcaac assess <process name> -f params.csv`.
            """.trimIndent())
    val arguments: Map<String, String> by option("-D", "--parameter")
            .help(
            """
                Override parameter value as a key value pair.
                Example: `lcaac assess <process name> -D x="12 kg" -D geo="UK" -f params.csv`.
            """.trimIndent())
            .associate()

    override fun run() {
        val files = lcaFiles(path)
        val symbolTable = Loader(BasicOperations).load(files, listOf(LoaderOption.WITH_PRELUDE))
        val processor = CsvProcessor(path, symbolTable)
        val iterator = loadRequests()
        val writer = CsvResultWriter()
        var first = true
        while (iterator.hasNext()) {
            val request = iterator.next()
            val results = processor.process(request)
            for (it in results) {
                if (first) {
                    echo(writer.header(it), trailingNewline = false)
                    first = false
                }
                echo(writer.row(it), trailingNewline = false)
            }
        }
    }

    private fun loadRequests(): Iterator<CsvRequest> {
        return file?.let { loadRequestsFrom(it) }
                ?: listOf(defaultRequest()).iterator()
    }

    private fun loadRequestsFrom(file: File): Iterator<CsvRequest> {
        val reader = CsvRequestReader(name, labels, file.inputStream(), arguments)
        return reader.iterator()
    }

    private fun defaultRequest(): CsvRequest {
        val pairs = arguments.toList()
        val header = pairs.mapIndexed { index, pair -> pair.first to index }.toMap()
        val record = pairs.map { it.second }
        return CsvRequest(
                name,
                labels,
                header,
                record,
        )
    }
}
