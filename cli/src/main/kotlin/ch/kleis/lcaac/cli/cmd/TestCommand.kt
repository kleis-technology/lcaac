package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.datasource.CsvSourceOperations
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.testing.BasicTestRunner
import ch.kleis.lcaac.core.testing.GenericFailure
import ch.kleis.lcaac.core.testing.RangeAssertionFailure
import ch.kleis.lcaac.core.testing.RangeAssertionSuccess
import ch.kleis.lcaac.grammar.CoreTestMapper
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

private const val greenTick = "\u2705"
private const val redCross = "\u274C"

class TestCommand : CliktCommand(name = "test", help = "Run specified tests") {
    val path: File by option("-p", "--path").file(canBeFile = false).default(File(".")).help("Path to root folder.")
    val file: File? by option("-f", "--file").file(canBeDir = false)
            .help("""
                CSV file with parameter values.
                Example: `lcaac assess <process name> -f params.csv`.
            """.trimIndent())
    val showSuccess: Boolean by option("--show-success").flag(default = false).help("Show successful assertions")

    override fun run() {
        val files = lcaFiles(path)
        val ops = BasicOperations
        val symbolTable = Loader(ops).load(files, listOf(LoaderOption.WITH_PRELUDE))
        val mapper = CoreTestMapper()
        val cases = files
            .flatMap { it.testDefinition() }
            .map { mapper.test(it) }
        val runner = BasicTestRunner<LcaLangParser.TestDefinitionContext>(
            symbolTable,
            CsvSourceOperations(path, ops))
        val results = cases.map { runner.run(it) }

        results.forEach { result ->
            result.results.forEachIndexed { id, assertion ->
                val isSuccess: Boolean = assertion is RangeAssertionSuccess
                val tick = if (isSuccess) greenTick else redCross

                val message = when (assertion) {
                    is GenericFailure -> assertion.message
                    is RangeAssertionFailure -> "${assertion.name} = ${assertion.actual} is not in between ${assertion.lo} and ${assertion.hi}"
                    is RangeAssertionSuccess -> "${assertion.name} = ${assertion.actual} is in between ${assertion.lo} and ${assertion.hi}"
                }
                if ((isSuccess && showSuccess) || !isSuccess)
                    echo("$tick  ${result.name}[$id] $message")
            }
        }
        val nbTests = results.flatMap { it.results }.count()
        val nbSuccesses = results.flatMap { it.results }.count { it is RangeAssertionSuccess }
        val nbFailures = nbTests - nbSuccesses
        echo("Run $nbTests tests, $nbSuccesses passed, $nbFailures failed")
        if (nbFailures > 0) throw ProgramResult(1)
    }
}
