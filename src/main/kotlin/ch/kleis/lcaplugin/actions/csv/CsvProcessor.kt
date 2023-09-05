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

    fun process(request: CsvRequest): List<CsvResult> {
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
        val entryPoint = trace.getEntryPoint()
        val program = ContributionAnalysisProgram(systemValue, entryPoint)
        val analysis = program.run()
        return entryPoint.products
            .map { output ->
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
