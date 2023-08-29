package ch.kleis.lcaplugin.actions.csv

import ch.kleis.lcaplugin.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import java.lang.Double.parseDouble

class CsvProcessor(
    private val symbolTable: SymbolTable<BasicNumber>,
) {
    private val ops = BasicOperations
    private val evaluator = Evaluator(symbolTable, ops)

    fun process(request: CsvRequest): CsvResult {
        val processName = request.processName
        val template = symbolTable.getTemplate(processName)!!
        val arguments = template.params
            .mapValues { entry ->
                when (val v = entry.value) {
                    is QuantityExpression<*> -> request[entry.key]?.let {
                        val amount = parseDouble(it)
                        EQuantityScale(ops.pure(amount), EUnitOf(v))
                    } ?: entry.value

                    is StringExpression -> request[entry.key]?.let {
                        EStringLiteral(it)
                    } ?: entry.value

                    else -> throw EvaluatorException("$v is not a supported data expression")
                }
            }

        val trace = evaluator.trace(EProcessTemplateApplication(template, arguments))
        val systemValue = trace.getSystemValue()
        val firstProcess = trace.getEntryPoint()
        val assessment = ContributionAnalysisProgram(systemValue, firstProcess, ops)
        val inventory = assessment.run()
        val outputPort =
            firstProcess.products.firstOrNull()
                ?.product
                ?: throw EvaluatorException("$processName has no products")
        val impacts = inventory.impactFactors.rowAsMap(outputPort)
        return CsvResult(
            request,
            outputPort,
            impacts,
        )
    }
}
