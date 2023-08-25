package ch.kleis.lcaplugin.actions.csv

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.math.QuantityOperations
import java.lang.Double.parseDouble

class CsvProcessor<Q>(
    private val symbolTable: SymbolTable<Q>,
    private val ops: QuantityOperations<Q>,
) {
    private val evaluator = Evaluator(symbolTable, ops)

    fun process(request: CsvRequest): CsvResult<Q> {
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
        val assessment = Assessment(systemValue, firstProcess, ops)
        val inventory = assessment.inventory()
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
