package ch.kleis.lcaplugin.actions.csv

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.value.ProductValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import java.lang.Double.parseDouble

class CsvProcessor(
    private val symbolTable: SymbolTable,
) {
    private val evaluator = Evaluator(symbolTable)

    fun process(request: CsvRequest): CsvResult {
        val processName = request.processName
        val template = symbolTable.getTemplate(processName)!!
        val arguments = template.params
            .mapValues { entry ->
                when (val v = entry.value) {
                    is QuantityExpression -> request[entry.key]?.let {
                        val amount = parseDouble(it)
                        EQuantityScale(amount, EUnitOf(v))
                    } ?: entry.value

                    is StringExpression -> request[entry.key]?.let {
                        EStringLiteral(it)
                    } ?: entry.value

                    else -> throw EvaluatorException("$v is not a supported data expression")
                }
            }

        val systemValue = evaluator.eval(EProcessTemplateApplication(template, arguments))
        val assessment = Assessment(systemValue)
        val inventory = assessment.inventory()
        val outputPort =
            systemValue.firstProductOf(processName) ?: throw EvaluatorException("$processName has no products")
        val impacts = inventory.rowAsMap(outputPort)
        return CsvResult(
            request,
            outputPort,
            impacts,
        )
    }
}

private fun SystemValue.firstProductOf(processName: String): ProductValue? {
    return this.getProcesses()
        .firstOrNull { it.name == processName }
        ?.products
        ?.map { it.product }
        ?.firstOrNull()
}

