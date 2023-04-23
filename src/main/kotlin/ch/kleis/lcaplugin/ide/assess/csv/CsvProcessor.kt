package ch.kleis.lcaplugin.actions.csv

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaplugin.core.lang.expression.EQuantityLiteral
import ch.kleis.lcaplugin.core.lang.expression.EUnitOf
import ch.kleis.lcaplugin.core.lang.value.*
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
                        EQuantityLiteral(amount, EUnitOf(entry.value))
                    } ?: entry.value
            }
        val systemValue = evaluator.eval(EProcessTemplateApplication(template, arguments))
        val assessment = Assessment(systemValue)
        val inventory = assessment.inventory()
        val outputPort =
            systemValue.firstProductOf(processName) ?: throw EvaluatorException("$processName has no products")
        val impacts = inventory.row(outputPort)
            .associate { it.input.port() to renderCf(it, it.input.port(), outputPort) }
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

private fun renderCf(
    cf: CharacterizationFactorValue,
    inputPort: MatrixColumnIndex,
    outputPort: MatrixColumnIndex,
): QuantityValue {
    val numerator = cf.input.quantity().referenceValue() / inputPort.referenceUnit().scale
    val denominator = cf.output.quantity().referenceValue() / outputPort.referenceUnit().scale
    return QuantityValue(numerator / denominator, inputPort.referenceUnit())
}
