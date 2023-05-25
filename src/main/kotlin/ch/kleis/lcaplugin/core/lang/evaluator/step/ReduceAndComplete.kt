package ch.kleis.lcaplugin.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.Helper
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.LcaExpressionReducer
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.TemplateExpressionReducer
import ch.kleis.lcaplugin.core.lang.expression.*

class ReduceAndComplete(
    symbolTable: SymbolTable,
) {
    private val processTemplates = symbolTable.processTemplates
    private val lcaReducer = LcaExpressionReducer(
        symbolTable.quantities,
    )
    private val templateReducer = TemplateExpressionReducer(
        symbolTable.quantities,
        processTemplates,
    )

    fun apply(expression: ProcessTemplateExpression): ProcessTemplateExpression {
        val reduced = when (expression) {
            is EProcessTemplateApplication -> templateReducer.reduce(expression)
            is EProcessFinal -> expression
            is EProcessTemplate -> templateReducer.reduce(EProcessTemplateApplication(expression, emptyMap()))
            is EProcessTemplateRef -> processTemplates[expression.name]?.let {
                templateReducer.reduce(EProcessTemplateApplication(expression, emptyMap()))
            } ?: expression
        }
        val unboundedReferences = Helper().allRequiredRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return completeSubstances(completeInputs(reduced))
    }

    fun apply(expression: ESubstanceCharacterization): ESubstanceCharacterization {
        val reduced = lcaReducer.reduceSubstanceCharacterization(expression)
        val unboundedReferences = Helper().allRequiredRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return completeIndicators(reduced)
    }


    private fun completeInputs(reduced: ProcessTemplateExpression): ProcessTemplateExpression {
        return (ProcessTemplateExpression.eProcessFinal.expression.inputs compose Every.list())
            .modify(reduced) { exchange ->
                val quantityExpression = exchange.quantity
                val referenceUnit = when {
                    quantityExpression is EUnitLiteral -> quantityExpression

                    quantityExpression is EQuantityScale && quantityExpression.base is EUnitLiteral ->
                        quantityExpression.base

                    else -> throw EvaluatorException("quantity $quantityExpression is not reduced")
                }

                ETechnoExchange.product
                    .modify(exchange) {
                        it.copy(referenceUnit = referenceUnit)
                    }
            }
    }

    private fun completeSubstances(reduced: ProcessTemplateExpression): ProcessTemplateExpression {
        return (ProcessTemplateExpression.eProcessFinal.expression.biosphere compose Every.list())
            .modify(reduced) { exchange ->
                val quantityExpression = exchange.quantity
                val referenceUnit = when {
                    quantityExpression is EUnitLiteral -> quantityExpression

                    quantityExpression is EQuantityScale && quantityExpression.base is EUnitLiteral ->
                        quantityExpression.base

                    else -> throw EvaluatorException("quantity $quantityExpression is not reduced")
                }

                EBioExchange.substance
                    .modify(exchange) {
                        if (it.referenceUnit == null) {
                            it.copy(referenceUnit = referenceUnit)
                        } else it
                    }
            }
    }

    private fun completeIndicators(reduced: ESubstanceCharacterization): ESubstanceCharacterization {
        return (ESubstanceCharacterization.impacts compose Every.list())
            .modify(reduced) { exchange ->
                val quantityExpression = exchange.quantity
                val referenceUnit = when {
                    quantityExpression is EUnitLiteral -> quantityExpression

                    quantityExpression is EQuantityScale && quantityExpression.base is EUnitLiteral ->
                        quantityExpression.base

                    else -> throw EvaluatorException("quantity $quantityExpression is not reduced")
                }

                EImpact.indicator
                    .modify(exchange) {
                        EIndicatorSpec(it.name, referenceUnit)
                    }
            }
    }
}
