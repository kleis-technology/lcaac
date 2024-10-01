package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.cli.csv.CsvProcessor
import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.cli.csv.CsvRequestReader
import ch.kleis.lcaac.cli.csv.CsvResultWriter
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.resilio_db.ResilioDbConnectorKeys
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import com.charleskorn.kaml.decodeFromStream
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
class AssessCommand : CliktCommand(name = "assess", help = "Returns the unitary impacts of a process in CSV format") {
    val name: String by argument().help("Process name")
    val labels: Map<String, String> by option("-l", "--label")
        .help(
            """
                    Specify a process label as a key value pair.
                    Example: lcaac assess <process name> -l model="ABC" -l geo="FR".
                """.trimIndent())
        .associate()
    private val getProjectPath = option("-p", "--project").file()
        .default(File(defaultLcaacFilename))
        .help("Path to project folder or yaml file.")
    val projectPath: File by getProjectPath

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
    val globals: Map<String, String> by option("-G", "--global")
        .help(
            """
                Override global variable as a key value pair.
                Example: `lcaac assess <process name> -G x="12 kg"`.
            """.trimIndent()
        ).associate()

    override fun run() {
        val (workingDirectory, lcaacConfigFile) = parseProjectPath(projectPath)
        val yamlConfig = if (lcaacConfigFile.exists()) projectPath.inputStream().use {
            yaml.decodeFromStream(LcaacConfig.serializer(), it)
        }
        else LcaacConfig()
        val config = yamlConfig.modifyConnector(ResilioDbConnectorKeys.RDB_CONNECTOR_NAME) { connector ->
            connector.modifyOption(ResilioDbConnectorKeys.RDB_URL) { url ->
                System.getenv()[EnvVars.RESILIO_DB_URL.key] ?: url
            }.modifyOption(ResilioDbConnectorKeys.RDB_ACCESS_TOKEN) { accessToken ->
                System.getenv()[EnvVars.RESILIO_DB_ACCESS_TOKEN.key] ?: accessToken
            }
        }


        val files = lcaFiles(workingDirectory)
        val symbolTable = Loader(
            ops = BasicOperations,
            overriddenGlobals = dataExpressionMap(BasicOperations, globals),
        ).load(files, listOf(LoaderOption.WITH_PRELUDE))

        val processor = CsvProcessor(config, symbolTable, workingDirectory.path)
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
