package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.cli.csv.CsvRequestReader
import ch.kleis.lcaac.cli.csv.trace.TraceCsvProcessor
import ch.kleis.lcaac.cli.csv.trace.TraceCsvResultWriter
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
import kotlin.io.path.Path

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
class TraceCommand : CliktCommand(name = "trace", help = "Trace the contributions") {
    val name: String by argument().help("Process name")
    val labels: Map<String, String> by option("-l", "--label")
        .help(
            """
                    Specify a process label as a key value pair.
                    Example: lcaac assess <process name> -l model="ABC" -l geo="FR".
                """.trimIndent())
        .associate()
    val configFile: File by option("-c", "--config", help = "Path to LCAAC config file").file(canBeDir = false)
        .default(File(defaultLcaacFilename))
        .help("Path to LCAAC config file. Defaults to 'lcaac.yaml'")

    val file: File? by option("-f", "--file").file(canBeDir = false)
        .help("""
                CSV file with parameter values.
                Example: `lcaac trace <process name> -f params.csv`.
            """.trimIndent())
    val arguments: Map<String, String> by option("-D", "--parameter")
        .help(
            """
                Override parameter value as a key value pair.
                Example: `lcaac assess <process name> -D x="12 kg" -D geo="UK" -f params.csv`.
            """.trimIndent())
        .associate()
    val globals: Map<String, String> by option("-G", "--global")
        .help(
            """
                Override global variable as a key value pair.
                Example: `lcaac assess <process name> -G x="12 kg"`.
            """.trimIndent()
        ).associate()

    override fun run() {
        val workingDirectory = Path(".").toFile()
        val yamlConfig = parseLcaacConfig(configFile)

        val files = lcaFiles(workingDirectory)
        val symbolTable = Loader(
            ops = BasicOperations,
            overriddenGlobals = dataExpressionMap(BasicOperations, globals),
        ).load(files, listOf(LoaderOption.WITH_PRELUDE))

        val processor = TraceCsvProcessor(yamlConfig, symbolTable, workingDirectory.path)
        val iterator = loadRequests()
        val writer = TraceCsvResultWriter()
        var first = true
        while (iterator.hasNext()) {
            val request = iterator.next()
            val result = processor.process(request)
            if (first) {
                echo(writer.header(result), trailingNewline = false)
                first = false
            }
            writer.rows(result).forEach {
                echo(it, trailingNewline = false)
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
