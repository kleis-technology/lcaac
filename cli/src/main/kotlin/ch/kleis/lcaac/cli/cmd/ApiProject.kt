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
 * A model with the API declared in its configuration (under the key 'api' of lcaac.yaml), checked against it.
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
    if (!configFile.isFile) throw CliktError("configuration not found: ${configFile.path}")
    val config = parseLcaacConfig(configFile)
    val sourceName = configFile.relativeToOrNull(File(".").absoluteFile)?.path ?: configFile.path
    val located = try {
        ApiDefinitionLoader.load(configFile.readText(), sourceName)
    } catch (e: ApiDefinitionException) {
        throw CliktError(e.message, e)
    } ?: throw CliktError("no API: declare it under the key 'api' of $sourceName")

    val symbolTable = Loader(BasicOperations).load(lcaFiles(sourceDirectory, source), listOf(LoaderOption.WITH_PRELUDE))
    val errors = ApiChecker(symbolTable).check(located)
    if (errors.isNotEmpty()) throw CliktError(errors.joinToString("\n"))
    return ApiProject(located.definition, symbolTable, config, projectDirectory)
}
