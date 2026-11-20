package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.server.api.OpenApiGenerator
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File

class ApiCommand : NoOpCliktCommand(name = "api", help = "Work with the API declared in the configuration")

@Suppress("MemberVisibilityCanBePrivate")
class OpenApiCommand : CliktCommand(
    name = "openapi",
    help = "Print the OpenAPI document of the API declared in the configuration",
) {
    val configFile: File by configFileOption()
    val source: File by sourceOption()

    @OptIn(ExperimentalSerializationApi::class)
    override fun run() {
        val project = loadApiProject(configFile, source)
        val document = OpenApiGenerator(project.symbolTable).generate(project.definition)
        val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
        echo(json.encodeToString(JsonObject.serializer(), document))
    }
}
