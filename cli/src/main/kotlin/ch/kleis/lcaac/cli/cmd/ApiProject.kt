package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.server.api.ApiChecker
import ch.kleis.lcaac.server.api.ApiDefinition
import ch.kleis.lcaac.server.api.ApiDefinitionException
import ch.kleis.lcaac.server.api.ApiDefinitionLoader
import com.github.ajalt.clikt.core.CliktError
import java.io.File

/**
 * A model with the API declared in its configuration (e.g. 'api: api.yaml' in lcaac.yaml), checked against it.
 */
class ApiProject(
    val definition: ApiDefinition,
    val symbolTable: SymbolTable<BasicNumber>,
    val config: LcaacConfig,
    val projectDirectory: File,
)

fun loadApiProject(configFile: File, source: File): ApiProject {
    val sourceDirectory = parseSource(source)
    val projectDirectory = configFile.parentFile
    val config = parseLcaacConfig(configFile)
    val apiPath = config.api
        ?: throw CliktError("no API definition: add 'api: <file>' to ${configFile.path}")
    val apiFile = File(projectDirectory, apiPath)
    if (!apiFile.isFile) throw CliktError("API definition not found: ${apiFile.path}")

    val symbolTable = Loader(BasicOperations).load(lcaFiles(sourceDirectory, source), listOf(LoaderOption.WITH_PRELUDE))
    val sourceName = apiFile.relativeToOrNull(File(".").absoluteFile)?.path ?: apiFile.path
    val located = try {
        ApiDefinitionLoader.load(apiFile.readText(), sourceName)
    } catch (e: ApiDefinitionException) {
        throw CliktError(e.message, e)
    }
    val errors = ApiChecker(symbolTable).check(located)
    if (errors.isNotEmpty()) throw CliktError(errors.joinToString("\n"))
    return ApiProject(located.definition, symbolTable, config, projectDirectory)
}
