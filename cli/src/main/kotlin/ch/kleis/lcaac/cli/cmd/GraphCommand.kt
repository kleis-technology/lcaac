package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.cli.mermaid.MermaidGraph
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import java.io.File

const val graphCommandName = "graph"

class GraphCommand : CliktCommand(name = graphCommandName, help = "Generate a Mermaid graph of processes") {
    val name: String by argument().help("Process name")
    val configFile: File by configFileOption()
    val source: File by sourceOption()
    val labels: Map<String, String> by labelsOption(graphCommandName)
    val arguments: Map<String, String> by argumentsOption(graphCommandName)
    val globals: Map<String, String> by globalsOption(graphCommandName)

    override fun run() {
        val sourceDirectory = parseSource(source)
        val projectDirectory = configFile.parentFile
        val yamlConfig = parseLcaacConfig(configFile)

        val files = lcaFiles(sourceDirectory)
        val symbolTable = Loader(
            ops = BasicOperations,
            overriddenGlobals = dataExpressionMap(BasicOperations, globals),
        ).load(files, listOf(LoaderOption.WITH_PRELUDE))

        val factory = ConnectorFactory(
            projectDirectory.path,
            yamlConfig,
            BasicOperations,
            symbolTable,
            listOf(CsvConnectorBuilder()),
        )
        val sourceOps = DefaultDataSourceOperations(BasicOperations, yamlConfig, factory.buildConnectors())
        val dataReducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, BasicOperations, sourceOps)
        val evaluator = Evaluator(symbolTable, BasicOperations, sourceOps)

        val reqLabels = labels
        val template = symbolTable.getTemplate(name, reqLabels)
            ?: throw EvaluatorException("Could not get template for $name$reqLabels")
        val args = prepareArguments(dataReducer, template, arguments)
        val trace = evaluator.trace(template, args)

        echo(MermaidGraph(trace).render(), trailingNewline = false)
    }
}