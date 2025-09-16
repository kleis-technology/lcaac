package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.cli.csv.CsvRequestReader
import ch.kleis.lcaac.cli.csv.assess.AssessCsvProcessor
import ch.kleis.lcaac.cli.csv.assess.AssessCsvResultWriter
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import java.io.File

const val assessCommandName = "assess"

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
class AssessCommand : CliktCommand(name = assessCommandName, help = "Returns the unitary impacts of a process in CSV format") {
    val name: String by argument().help("Process name")
    val configFile: File by configFileOption()
    val source: File by sourceOption()
    val file: File? by fileOption(assessCommandName)
    val labels: Map<String, String> by labelsOption(assessCommandName)
    val arguments: Map<String, String> by argumentsOption(assessCommandName)
    val globals: Map<String, String> by globalsOption(assessCommandName)

    override fun run() {
        val sourceDirectory = parseSource(source)
        val projectDirectory = configFile.parentFile
        val yamlConfig = parseLcaacConfig(configFile)

        val files = lcaFiles(sourceDirectory)
        val symbolTable = Loader(
            ops = BasicOperations,
            overriddenGlobals = dataExpressionMap(BasicOperations, globals),
        ).load(files, listOf(LoaderOption.WITH_PRELUDE))

        val processor = AssessCsvProcessor(yamlConfig, symbolTable, projectDirectory.path)
        val iterator = loadRequests()
        val writer = AssessCsvResultWriter()
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
