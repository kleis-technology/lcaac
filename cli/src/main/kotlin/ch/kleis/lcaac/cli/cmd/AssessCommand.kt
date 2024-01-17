package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.cli.csv.CsvProcessor
import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.cli.csv.CsvRequestReader
import ch.kleis.lcaac.cli.csv.CsvResultWriter
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
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
    val labels: Map<String, String> by option("-l", "--label").associate()
    val root: File by option("-r", "--root").file(canBeFile = false).default(File(".")).help("Root folder")
    val data: File? by option("-d", "--data").file(canBeDir = false).help("CSV file with parameter values")

    override fun run() {
        val files = lcaFiles(root)
        val symbolTable = Loader(BasicOperations).load(files)
        val processor = CsvProcessor(symbolTable)
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
        return data?.let { loadRequestsFrom(it) }
            ?: listOf(CsvRequest(name, labels, emptyMap(), emptyList())).iterator()
    }

    private fun loadRequestsFrom(file: File): Iterator<CsvRequest> {
        val reader = CsvRequestReader(name, labels, file.inputStream())
        return reader.iterator()
    }
}
