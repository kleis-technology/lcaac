package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.lang.evaluator.AssertionFrame
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.testing.BasicTestRunner
import ch.kleis.lcaac.core.testing.GenericFailure
import ch.kleis.lcaac.core.testing.RangeAssertionFailure
import ch.kleis.lcaac.core.testing.RangeAssertionSuccess
import ch.kleis.lcaac.core.testing.TestCase
import ch.kleis.lcaac.grammar.CoreTestMapper
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import java.io.File

private const val greenTick = "\u2705"
private const val redCross = "\u274C"

const val testCommandName = "test"

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
class TestCommand : CliktCommand(name = testCommandName, help = "Run specified tests") {
    val name: String by argument().help("Process name").default("")
    val configFile: File by configFileOption()
    val source: File by sourceOption()
    val file: File? by fileOption(testCommandName)
    val showSuccess: Boolean by option("--show-success").flag(default = false).help("Show successful assertions")

    /** The assertion [id] of [case] and where it is written, if known. */
    private fun locationOf(case: TestCase<*>, id: Int): String? {
        val assertion = case.assertions.getOrNull(id) ?: return null
        val path = assertion.path ?: return null
        val location = case.sourceMap[path] ?: return null
        return "${AssertionFrame(path, assertion.ref).description} ($location)"
    }

    override fun run() {
        val sourceDirectory = parseSource(source)
        val projectDirectory = configFile.parentFile
        val yamlConfig = parseLcaacConfig(configFile)

        val ops = BasicOperations
        val files = lcaFiles(sourceDirectory, source)
        val symbolTable = Loader(ops).load(files, listOf(LoaderOption.WITH_PRELUDE))

        val factory = ConnectorFactory(
            projectDirectory.path,
            yamlConfig,
            ops,
            symbolTable,
            listOf(CsvConnectorBuilder())
        )
        val sourceOps = DefaultDataSourceOperations(ops,yamlConfig, factory.buildConnectors())

        val mapper = CoreTestMapper()
        val cases = files
            .flatMap { it.testDefinition() }
            .map { mapper.test(it) }
            .filter { name.isBlank() || it.name == name }
        val runner = BasicTestRunner<LcaLangParser.TestDefinitionContext>(
            symbolTable,
            sourceOps,
        )
        val results = cases.map { runner.run(it) }

        cases.zip(results).forEach { (case, result) ->
            result.results.forEachIndexed { id, assertion ->
                val isSuccess: Boolean = assertion is RangeAssertionSuccess
                val tick = if (isSuccess) greenTick else redCross

                val message = when (assertion) {
                    is GenericFailure -> assertion.message
                    is RangeAssertionFailure -> "${assertion.name} = ${assertion.actual} is not in between ${assertion.lo} and ${assertion.hi}"
                    is RangeAssertionSuccess -> "${assertion.name} = ${assertion.actual} is in between ${assertion.lo} and ${assertion.hi}"
                }
                if ((isSuccess && showSuccess) || !isSuccess) {
                    echo("$tick  ${result.name}[$id] $message")
                    // Where an evaluation error occurred, one line per frame, or else where the assertion is
                    val error = (assertion as? GenericFailure)?.error
                    if (error != null) error.describe().lines().drop(1).forEach { echo("   $it") }
                    else if (!isSuccess) locationOf(case, id)?.let { echo("     in $it") }
                }
            }
        }
        val nbTests = results.flatMap { it.results }.count()
        val nbSuccesses = results.flatMap { it.results }.count { it is RangeAssertionSuccess }
        val nbFailures = nbTests - nbSuccesses
        echo("Run $nbTests tests, $nbSuccesses passed, $nbFailures failed")
        if (nbFailures > 0) throw ProgramResult(1)
    }
}
