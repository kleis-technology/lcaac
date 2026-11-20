package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.server.api.ApiChecker
import ch.kleis.lcaac.server.api.ApiDefinitionException
import ch.kleis.lcaac.server.api.ApiDefinitionLoader
import ch.kleis.lcaac.server.http.serve
import ch.kleis.lcaac.server.run.OperationRunner
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import java.io.File

const val serveCommandName = "serve"

@Suppress("MemberVisibilityCanBePrivate")
class ServeCommand : CliktCommand(
    name = serveCommandName,
    help = "Serve over HTTP the API declared in the configuration (e.g. 'api: api.yaml' in lcaac.yaml)",
) {
    val configFile: File by configFileOption()
    val source: File by sourceOption()
    val host: String by option("--host", help = "Host to listen on").default("127.0.0.1")
    val port: Int by option("-p", "--port", help = "Port to listen on").int().default(8080)
    val hideLocations: Boolean by option(
        "--hide-locations",
        help = "Do not show the locations of the model in the errors returned to clients",
    ).flag()

    override fun run() {
        val sourceDirectory = parseSource(source)
        val projectDirectory = configFile.parentFile
        val yamlConfig = parseLcaacConfig(configFile)
        val apiPath = yamlConfig.api
            ?: throw CliktError("no API definition: add 'api: <file>' to ${configFile.path}")
        val apiFile = File(projectDirectory, apiPath)
        if (!apiFile.isFile) throw CliktError("API definition not found: ${apiFile.path}")

        val ops = BasicOperations
        val symbolTable = Loader(ops).load(lcaFiles(sourceDirectory, source), listOf(LoaderOption.WITH_PRELUDE))
        val sourceName = apiFile.relativeToOrNull(File(".").absoluteFile)?.path ?: apiFile.path
        val located = try {
            ApiDefinitionLoader.load(apiFile.readText(), sourceName)
        } catch (e: ApiDefinitionException) {
            throw CliktError(e.message, e)
        }
        val errors = ApiChecker(symbolTable).check(located)
        if (errors.isNotEmpty()) throw CliktError(errors.joinToString("\n"))

        val connectors = ConnectorFactory(projectDirectory.path, yamlConfig, ops, symbolTable, listOf(CsvConnectorBuilder()))
            .buildConnectors()
        val runner = OperationRunner(symbolTable, yamlConfig, connectors, showLocations = !hideLocations)
        val api = located.definition.api
        echo("Serving ${api.name} ${api.version} on http://$host:$port", err = true)
        serve(located.definition, runner, host, port)
    }
}
