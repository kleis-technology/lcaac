package ch.kleis.lcaac.cli.csv.trace

import ch.kleis.lcaac.grammar.parseOverrides
import ch.kleis.lcaac.grammar.prepareArguments
import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.core.assessment.TraceTableProgram
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
    private val sourceOps = DefaultDataSourceOperations(ops, config, factory.buildConnectors())

    fun process(request: CsvRequest): TraceCsvResult {
        val initialDataReducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, sourceOps)
        val globalParameters = parseOverrides(
            initialDataReducer,
            symbolTable.globalParameters.toMap().mapKeys { it.key.name },
            request.toMap(),
        )
        val reqName = request.processName
        val reqLabels = request.matchLabels
        val template = symbolTable.getTemplate(reqName, reqLabels)
            ?: throw EvaluatorException("Could not get template for ${reqName}${reqLabels}")
        val processParameters = prepareArguments(initialDataReducer, template, request.toMap())

        val st = symbolTable.overrideGlobalParameters(globalParameters)
        val evaluator = Evaluator(st, ops, sourceOps)
        val trace = evaluator.trace(template, processParameters)
        val items = TraceTableProgram(trace).run()
        return TraceCsvResult(
            request,
            trace = items,
        )
    }
}
