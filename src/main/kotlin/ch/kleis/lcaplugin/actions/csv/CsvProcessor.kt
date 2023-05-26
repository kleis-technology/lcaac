package ch.kleis.lcaplugin.actions.csv

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaplugin.core.lang.expression.EQuantityScale
import ch.kleis.lcaplugin.core.lang.expression.EUnitOf
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
                request[entry.key]
                    ?.let {
                        val amount = parseDouble(it)
                        EQuantityScale(amount, EUnitOf(entry.value))
                    } ?: entry.value
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
    return this.processes
        .firstOrNull { it.name == processName }
        ?.products
        ?.map { it.product }
        ?.firstOrNull()
}

