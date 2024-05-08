package ch.kleis.lcaac.cli.csv

import ch.kleis.lcaac.cli.cmd.prepareArguments
import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations

class CsvProcessor(
    config: LcaacConfig,
    private val symbolTable: SymbolTable<BasicNumber>,
) {
    private val ops = BasicOperations
    private val sourceOps = DefaultDataSourceOperations(config, ops)
    private val dataReducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, sourceOps)
    private val evaluator = Evaluator(symbolTable, ops, sourceOps)

    fun process(request: CsvRequest): List<CsvResult> {
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
        return entryPoint.products.map { output ->
                val outputPort = output.product
                val impacts = analysis.getUnitaryImpacts(outputPort)
                CsvResult(
                    request,
                    outputPort,
                    impacts,
                )
            }
    }

}
