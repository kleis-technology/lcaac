package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.CsvSourceOperations
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.value.FullyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.lang.value.PartiallyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude.Companion.sanitize
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File

class TraceCommand : CliktCommand(name = "trace", help = "Trace the contributions") {
    val name: String by argument().help("Process name")
    val labels: Map<String, String> by option("-l", "--label")
        .help(
            """
                    Specify a process label as a key value pair.
                    Example: lcaac assess <process name> -l model="ABC" -l geo="FR".
                """.trimIndent())
        .associate()
    private val getPath = option("-p", "--path").file(canBeFile = false).default(File(".")).help("Path to root folder.")
    val path: File by getPath
    val dataSourcePath: File by option("--data-path").file(canBeFile = false)
        .defaultLazy { getPath.value }
        .help("Path to data folder. Default to root folder.")
    val arguments: Map<String, String> by option("-D", "--parameter")
        .help(
            """
                Override parameter value as a key value pair.
                Example: `lcaac assess <process name> -D x="12 kg" -D geo="UK" -f params.csv`.
            """.trimIndent())
        .associate()

    override fun run() {
        val ops = BasicOperations
        val sourceOps = CsvSourceOperations(dataSourcePath, ops)
        val files = lcaFiles(path)
        val symbolTable = Loader(ops).load(files, listOf(LoaderOption.WITH_PRELUDE))
        val evaluator = Evaluator(symbolTable, ops, sourceOps)
        val template = symbolTable.getTemplate(name, labels)
            ?: throw EvaluatorException("unknown template $name$labels")
        val args = prepareArguments(
            DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, sourceOps),
            template,
            arguments,
        )
        val trace = evaluator.trace(template, args)
        val system = trace.getSystemValue()
        val entryPoint = trace.getEntryPoint()

        val program = ContributionAnalysisProgram(system, entryPoint)
        val analysis = program.run()

        val observablePorts = analysis.getObservablePorts()
            .getElements()
            .sortedWith(trace.getComparator())
        val controllablePorts = analysis.getControllablePorts().getElements()
            .sortedBy { it.getShortName() }

        val header = listOf(
            "name", "a", "b", "c", "amount", "unit",
        ).plus(
            controllablePorts.flatMap {
                listOf(
                    sanitize(it.getDisplayName()),
                    "${sanitize(it.getDisplayName())}_unit"
                )
            }
        )

        val lines = observablePorts.asSequence()
            .map { row ->
                val supply = analysis.supplyOf(row)
                val prefix = when (row) {
                    is IndicatorValue -> {
                        listOf(
                            row.name,
                            "",
                            "",
                            "",
                            supply.amount.toString(),
                            supply.unit.toString(),
                        )
                    }

                    is ProductValue -> {
                        listOf(
                            row.name,
                            row.fromProcessRef?.name ?: "",
                            row.fromProcessRef?.matchLabels?.toString() ?: "",
                            row.fromProcessRef?.arguments?.toString() ?: "",
                            supply.amount.toString(),
                            supply.unit.toString(),
                        )
                    }

                    is FullyQualifiedSubstanceValue -> {
                        listOf(
                            row.name,
                            row.compartment,
                            row.subcompartment ?: "",
                            row.type.toString(),
                            supply.amount.toString(),
                            supply.unit.toString(),
                        )
                    }

                    is PartiallyQualifiedSubstanceValue -> {
                        listOf(
                            row.name,
                            "",
                            "",
                            "",
                            supply.amount.toString(),
                            supply.unit.toString(),
                        )
                    }
                }
                val impacts = controllablePorts.flatMap { col ->
                    val impact = analysis.getPortContribution(row, col)
                    listOf(
                        impact.amount.toString(),
                        impact.unit.toString(),
                    )
                }
                prefix.plus(impacts)
            }

        val s = StringBuilder()
        CSVPrinter(s, format).printRecord(header)
        echo(s.toString(), trailingNewline = false)
        lines
            .forEach {
                s.clear()
                CSVPrinter(s, format).printRecord(it)
                echo(s.toString(), trailingNewline = false)
            }
    }

    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setRecordSeparator(System.lineSeparator())
        .build()
}
