package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.cli.explain.ExplanationRenderer
import ch.kleis.lcaac.cli.explain.NumberFormat
import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.explain.ExplainOptions
import ch.kleis.lcaac.core.explain.ExplanationBuilder
import ch.kleis.lcaac.core.lang.dimension.UnitSimplifier
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorOptions
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import java.io.File

const val explainCommandName = "explain"

class ExplainCommand : CliktCommand(
    name = explainCommandName,
    help = "Explain how the impacts of a process are obtained, step by step, from its parameters, " +
        "variables and data sources to its results",
) {
    val name: String by argument().help("Process name")
    val configFile: File by configFileOption()
    val source: File by sourceOption()
    val labels: Map<String, String> by labelsOption(explainCommandName)
    val arguments: Map<String, String> by argumentsOption(explainCommandName)
    val globals: Map<String, String> by globalsOption(explainCommandName)
    val indicators: List<String> by option("-i", "--indicators", help = "Only explain these indicators (repeatable)").multiple()
    val depth: Int? by option("--depth", help = "Only explain the processes up to this depth in the supply chain (the process is at 0)")
        .int().restrictTo(min = 0)
    val precision: Int by option("--precision", help = "Number of significant digits (default 6)")
        .int().restrictTo(min = 1, max = 17).default(6)
    val maxRows: Int by option("--max-rows", help = "Maximum number of data source rows listed for a read (default 20)")
        .int().restrictTo(min = 0).default(20)

    override fun run() {
        val ops = BasicOperations
        val sourceDirectory = parseSource(source)
        val projectDirectory = configFile.parentFile
        val config = parseLcaacConfig(configFile)

        val symbolTable = Loader(
            ops = ops,
            overriddenGlobals = dataExpressionMap(ops, globals),
        ).load(lcaFiles(sourceDirectory, source), listOf(LoaderOption.WITH_PRELUDE)).withGivenGlobals(globals)
        val factory = ConnectorFactory(projectDirectory.path, config, ops, symbolTable, listOf(CsvConnectorBuilder()))
        val sourceOps = DefaultDataSourceOperations(ops, config, factory.buildConnectors())

        // Arguments are handled as in assess and trace
        val dataReducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, sourceOps)
        val globalParameters = parseOverrides(
            dataReducer,
            symbolTable.globalParameters.toMap().mapKeys { it.key.name },
            arguments,
        )
        val template = symbolTable.getTemplate(name, labels)
            ?: throw EvaluatorException("Could not get template for $name$labels")
        val processArguments = prepareArguments(dataReducer, template, arguments)

        val evaluated = symbolTable.overrideGlobalParameters(globalParameters)
        val trace = Evaluator(evaluated, ops, sourceOps, EvaluatorOptions(recordDerivations = true))
            .trace(template, processArguments)
        val analysis = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint()).run()
        val explanation = ExplanationBuilder<BasicNumber, BasicMatrix>(ops, ExplainOptions(indicators.toSet(), depth))
            .build(trace, analysis)

        val renderer = ExplanationRenderer(evaluated.sourceMap, UnitSimplifier.of(evaluated, ops), NumberFormat(precision), maxRows)
        echo(renderer.render(explanation), trailingNewline = false)
    }
}
