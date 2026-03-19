package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.cli.mermaid.ImpactMode
import ch.kleis.lcaac.cli.mermaid.MermaidGraph
import ch.kleis.lcaac.cli.mermaid.MermaidGraphOption
import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
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
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.io.File

const val graphCommandName = "graph"

enum class GraphFormat { MERMAID, HTML }

class GraphCommand : CliktCommand(name = graphCommandName, help = "Generate a Mermaid graph of processes") {
    val name: String by argument().help("Process name")
    val configFile: File by configFileOption()
    val source: File by sourceOption()
    val labels: Map<String, String> by labelsOption(graphCommandName)
    val arguments: Map<String, String> by argumentsOption(graphCommandName)
    val globals: Map<String, String> by globalsOption(graphCommandName)
    val hideProducts: Boolean by option("--hide-products", help = "Hide product names on edges").flag(default = false)
    val showQuantities: Boolean by option("--show-quantities", help = "Show quantities on edges").flag(default = false)
    val showBiosphere: Boolean by option("--show-biosphere", help = "Show biosphere edges").flag(default = false)
    val showImpacts: Boolean by option("--show-impacts", help = "Show impact edges").flag(default = false)
    val indicatorName: String? by option("-i", "--indicator", help = "Show port contribution for this indicator on each edge")
    val absolute: Boolean by option("--absolute", help = "Report indicator contributions in absolute values (default: relative %)").flag(default = false)
    val outputFormat: GraphFormat by option("-o", "--output", help = "Output format (mermaid or html)")
        .choice("mermaid" to GraphFormat.MERMAID, "html" to GraphFormat.HTML)
        .default(GraphFormat.MERMAID)

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

        val graphOptions = buildSet {
            if (hideProducts) add(MermaidGraphOption.HIDE_PRODUCTS)
            if (showQuantities) add(MermaidGraphOption.SHOW_QUANTITIES)
            if (showBiosphere) add(MermaidGraphOption.SHOW_BIOSPHERE)
            if (showImpacts) add(MermaidGraphOption.SHOW_IMPACTS)
        }
        val indicator = indicatorName?.let { name ->
            val system = trace.getSystemValue()
            val entryPoint = trace.getEntryPoint()
            val analysis = ContributionAnalysisProgram(system, entryPoint).run()
            analysis.getIndicators().firstOrNull { it.name == name }
                ?: throw EvaluatorException("Indicator not found: $name")
        }
        val impactMode = if (absolute) ImpactMode.ABSOLUTE else ImpactMode.RELATIVE
        val mermaid = MermaidGraph(trace, graphOptions, indicator, impactMode).render()
        val output = when (outputFormat) {
            GraphFormat.MERMAID -> mermaid
            GraphFormat.HTML -> renderHtml(mermaid)
        }
        echo(output, trailingNewline = false)
    }

    private fun renderHtml(mermaid: String): String = """
        <!DOCTYPE html>
        <html>
        <head><meta charset="utf-8"></head>
        <body>
        <pre class="mermaid">
        $mermaid</pre>
        <script type="module">
            import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs';
            mermaid.initialize({ startOnLoad: true });
        </script>
        </body>
        </html>
    """.trimIndent()
}
