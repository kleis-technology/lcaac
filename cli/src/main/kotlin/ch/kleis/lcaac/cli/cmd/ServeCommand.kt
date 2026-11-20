package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.server.api.OpenApiGenerator
import ch.kleis.lcaac.server.http.serve
import ch.kleis.lcaac.server.run.OperationRunner
import com.github.ajalt.clikt.core.CliktCommand
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
        val project = loadApiProject(configFile, source)
        val connectors = ConnectorFactory(
            project.projectDirectory.path, project.config, BasicOperations, project.symbolTable, listOf(CsvConnectorBuilder()),
        ).buildConnectors()
        val runner = OperationRunner(project.symbolTable, project.config, connectors, showLocations = !hideLocations)
        val openApi = OpenApiGenerator(project.symbolTable).generate(project.definition)
        val api = project.definition.api
        echo("Serving ${api.name} ${api.version} on http://$host:$port", err = true)
        serve(project.definition, runner, openApi, host, port)
    }
}
