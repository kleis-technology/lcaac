package ch.kleis.lcaplugin.core.lang.evaluator.compiler

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.LcaExpressionReducer
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.TemplateExpressionReducer
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.indicatorRefInIndicatorExpression
import ch.kleis.lcaplugin.core.lang.expression.optics.productRefInProductExpression
import ch.kleis.lcaplugin.core.lang.expression.optics.substanceRefInLcaSubstanceExpression

class ReduceAndComplete(
    symbolTable: SymbolTable,
) {
    private val processTemplates = symbolTable.processTemplates
    private val lcaReducer = LcaExpressionReducer(
        symbolTable.products,
        symbolTable.substances,
        symbolTable.indicators,
        symbolTable.quantities,
        symbolTable.units,
        symbolTable.substanceCharacterizations
    )
    private val templateReducer = TemplateExpressionReducer(
        symbolTable.products,
        symbolTable.substances,
        symbolTable.indicators,
        symbolTable.quantities,
        symbolTable.units,
        processTemplates,
    )

    fun apply(expression: TemplateExpression): TemplateExpression {
        val reduced = when (expression) {
            is EInstance -> templateReducer.reduce(expression)
            is EProcessFinal -> expression
            is EProcessTemplate -> templateReducer.reduce(EInstance(expression, emptyMap()))
            is ETemplateRef -> processTemplates[expression.name]?.let {
                templateReducer.reduce(EInstance(expression, emptyMap()))
            } ?: expression
        }
        val unboundedReferences = Helper().allRequiredRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return completeSubstances(completeInputs(reduced))
    }

    fun apply(expression: LcaSubstanceCharacterizationExpression): LcaSubstanceCharacterizationExpression {
        val reduced = lcaReducer.reduceSubstanceCharacterization(expression)
        val unboundedReferences = Helper().allRequiredRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return completeIndicators(reduced)
    }


    private fun completeInputs(reduced: TemplateExpression): TemplateExpression {
        return (TemplateExpression.eProcessFinal.expression.eProcess.inputs compose Every.list())
            .modify(reduced) { exchange ->
                val q = exchange.quantity
                if (q !is EQuantityLiteral) {
                    throw EvaluatorException("quantity $q is not reduced")
                }
                (ETechnoExchange.product compose productRefInProductExpression)
                    .modify(exchange) {
                        EProduct(it.name, q.unit)
                    }
            }
    }

    private fun completeSubstances(reduced: TemplateExpression): TemplateExpression {
        return (TemplateExpression.eProcessFinal.expression.eProcess.biosphere compose Every.list())
            .modify(reduced) { exchange ->
                val q = exchange.quantity
                if (q !is EQuantityLiteral) {
                    throw EvaluatorException("quantity $q is not reduced")
                }
                (EBioExchange.substance compose substanceRefInLcaSubstanceExpression)
                    .modify(exchange) {
                        ESubstance(
                            it.name,
                            it.name,
                            "__unknown__",
                            null,
                            q.unit,
                        )
                    }
            }
    }

    private fun completeIndicators(reduced: LcaSubstanceCharacterizationExpression): LcaSubstanceCharacterizationExpression {
        return (LcaSubstanceCharacterizationExpression.eSubstanceCharacterization.impacts compose Every.list())
            .modify(reduced) { exchange ->
                val q = exchange.quantity
                if (q !is EQuantityLiteral) {
                    throw EvaluatorException("quantity $q is not reduced")
                }
                (EImpact.indicator compose indicatorRefInIndicatorExpression)
                    .modify(exchange) {
                        EIndicator(it.name, q.unit)
                    }
            }
    }
}
