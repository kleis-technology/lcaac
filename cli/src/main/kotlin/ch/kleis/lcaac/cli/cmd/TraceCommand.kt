package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.value.FullyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.lang.value.PartiallyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.math.basic.BasicOperations.toDouble
import ch.kleis.lcaac.core.prelude.Prelude.Companion.sanitize
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import com.charleskorn.kaml.decodeFromStream
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
class TraceCommand : CliktCommand(name = "trace", help = "Trace the contributions") {
    val name: String by argument().help("Process name")
    val labels: Map<String, String> by option("-l", "--label")
        .help(
            """
                    Specify a process label as a key value pair.
                    Example: lcaac assess <process name> -l model="ABC" -l geo="FR".
                """.trimIndent())
        .associate()
    private val getProjectPath = option("-p", "--project").file()
        .default(File(defaultLcaacFilename))
        .help("Path to project folder or yaml file.")
    val projectPath: File by getProjectPath

    val arguments: Map<String, String> by option("-D", "--parameter")
        .help(
            """
                Override parameter value as a key value pair.
                Example: `lcaac assess <process name> -D x="12 kg" -D geo="UK" -f params.csv`.
            """.trimIndent())
        .associate()
    val globals: Map<String, String> by option("-G", "--global")
        .help(
            """
                Override global variable as a key value pair.
                Example: `lcaac assess <process name> -G x="12 kg"`.
            """.trimIndent()
        ).associate()

    override fun run() {
        val (workingDirectory, lcaacConfigFile) = parseProjectPath(projectPath)
        val yamlConfig = if (lcaacConfigFile.exists()) projectPath.inputStream().use {
            yaml.decodeFromStream(LcaacConfig.serializer(), it)
        }
        else LcaacConfig()

        val ops = BasicOperations
        val files = lcaFiles(workingDirectory)
        val symbolTable = Loader(
            ops = BasicOperations,
            overriddenGlobals = dataExpressionMap(BasicOperations, globals),
        ).load(files, listOf(LoaderOption.WITH_PRELUDE))

        val factory = ConnectorFactory(
            workingDirectory.path,
            yamlConfig,
            ops,
            symbolTable,
            listOf(CsvConnectorBuilder())
        )
        val sourceOps = DefaultDataSourceOperations(ops, yamlConfig, factory.buildConnectors())

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
            "depth", "d_amount", "d_unit", "d_product", "alloc",
            "name", "a", "b", "c", "amount", "unit",
        ).plus(
            controllablePorts.flatMap {
                listOf(
                    sanitize(it.getDisplayName()),
                    "${sanitize(it.getDisplayName())}_unit"
                )
            }
        )

        val products = entryPoint.products.asSequence()
        val lines = products.flatMap { demandedProduct ->
            val demandedAmount = demandedProduct.quantity.amount
            val demandedUnit = demandedProduct.quantity.unit
            val demandedProductName = demandedProduct.product.name
            val allocationAmount = (demandedProduct.allocation?.amount?.toDouble()
                ?: 1.0) * (demandedProduct.allocation?.unit?.scale ?: 1.0)
            observablePorts.asSequence()
                .map { row ->
                    val supply = analysis.supplyOf(row)
                    val depth = trace.getDepthOf(row)?.toString() ?: ""
                    val supplyAmount = supply.amount.value * allocationAmount
                    val prefix = when (row) {
                        is IndicatorValue -> {
                            listOf(
                                depth,
                                demandedAmount.toString(),
                                demandedUnit.toString(),
                                demandedProductName,
                                allocationAmount.toString(),
                                row.name,
                                "",
                                "",
                                "",
                                supplyAmount.toString(),
                                supply.unit.toString(),
                            )
                        }

                        is ProductValue -> {
                            listOf(
                                depth,
                                demandedAmount.toString(),
                                demandedUnit.toString(),
                                demandedProductName,
                                allocationAmount.toString(),
                                row.name,
                                row.fromProcessRef?.name ?: "",
                                row.fromProcessRef?.matchLabels?.toString() ?: "",
                                row.fromProcessRef?.arguments?.toString() ?: "",
                                supplyAmount.toString(),
                                supply.unit.toString(),
                            )
                        }

                        is FullyQualifiedSubstanceValue -> {
                            listOf(
                                depth,
                                demandedAmount.toString(),
                                demandedUnit.toString(),
                                demandedProductName,
                                allocationAmount.toString(),
                                row.name,
                                row.compartment,
                                row.subcompartment ?: "",
                                row.type.toString(),
                                supplyAmount.toString(),
                                supply.unit.toString(),
                            )
                        }

                        is PartiallyQualifiedSubstanceValue -> {
                            listOf(
                                depth,
                                demandedAmount.toString(),
                                demandedUnit.toString(),
                                demandedProductName,
                                allocationAmount.toString(),
                                row.name,
                                "",
                                "",
                                "",
                                supplyAmount.toString(),
                                supply.unit.toString(),
                            )
                        }
                    }
                    val impacts = controllablePorts.flatMap { col ->
                        val impact = analysis.getPortContribution(row, col)
                        val impactAmount = impact.amount.value * allocationAmount
                        listOf(
                            impactAmount.toString(),
                            impact.unit.toString(),
                        )
                    }
                    prefix.plus(impacts)
                }
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
