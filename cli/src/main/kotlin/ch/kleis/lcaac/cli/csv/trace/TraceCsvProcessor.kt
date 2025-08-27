package ch.kleis.lcaac.cli.csv.trace

import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
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
    private val dataReducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, sourceOps)
    private val evaluator = Evaluator(symbolTable, ops, sourceOps)

}
