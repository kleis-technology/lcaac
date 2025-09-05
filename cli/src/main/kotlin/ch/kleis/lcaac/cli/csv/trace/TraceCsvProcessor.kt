package ch.kleis.lcaac.cli.csv.trace

import ch.kleis.lcaac.cli.cmd.prepareArguments
import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations

class TraceCsvProcessor(
    config: LcaacConfig,
    private val symbolTable: SymbolTable<BasicNumber>,
    workingDirectory: String,
) {
    private val ops = BasicOperations
    private val factory = ConnectorFactory(
        workingDirectory,
        config,
        ops,
        symbolTable,
        listOf(CsvConnectorBuilder())
    )
    private val sourceOps = DefaultDataSourceOperations(ops, factory.buildConnectors())
    private val dataReducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, sourceOps)
    private val evaluator = Evaluator(symbolTable, ops, sourceOps)

    fun process(request: CsvRequest): TraceCsvResult {
        val reqName = request.processName
        val reqLabels = request.matchLabels
        val template = symbolTable.getTemplate(reqName, reqLabels)
            ?: throw EvaluatorException("Could not get template for ${reqName}${reqLabels}")
        val arguments = prepareArguments(dataReducer, template, request.toMap())
        val trace = evaluator.trace(template, arguments)
        val systemValue = trace.getSystemValue()
        val entryPoint = trace.getEntryPoint()
        val program = ContributionAnalysisProgram(systemValue, entryPoint)
        val analysis = program.run()
        val observablePorts = analysis.getObservablePorts()
            .getElements()
            .sortedWith(trace.getComparator())
        val controllablePorts = analysis.getControllablePorts().getElements()
            .sortedBy { it.getShortName() }
        val products = entryPoint.products.asSequence()

        val items = products.flatMap { demandedProduct ->
            observablePorts.asSequence().map {
                val depth = trace.getDepthOf(it)
                val supply = analysis.supplyOf(it)
                TraceCsvResultItem(
                    depth ?: -1,
                    demandedProduct,
                    supply,
                    it,
                    controllablePorts.associateWith { col ->
                        analysis.getPortContribution(it, col)
                    },
                )
            }
        }.toList()
        return TraceCsvResult(
            request,
            trace = items,
        )
    }
}
