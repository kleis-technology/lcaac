package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.LcaExpressionReducer
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.TemplateExpressionReducer
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.Merge
import ch.kleis.lcaplugin.core.lang.expression.optics.indicatorRefInIndicatorExpression
import ch.kleis.lcaplugin.core.lang.expression.optics.productRefInProductExpression

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
        val unboundedReferences = Helper().allUnboundedReferencesButProductRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return completeProducts(reduced)
    }

    fun apply(expression: LcaSubstanceCharacterizationExpression): LcaSubstanceCharacterizationExpression {
        val reduced = lcaReducer.reduceSubstanceCharacterization(expression)
        val unboundedReferences = Helper().allUnboundedReferencesButIndicatorRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return completeIndicators(reduced)
    }




    private fun completeProducts(reduced: TemplateExpression): TemplateExpression {
        return Merge(
            listOf(
                TemplateExpression.eProcessFinal.expression.eProcess.products compose Every.list(),
                TemplateExpression.eProcessFinal.expression.eProcess.inputs compose Every.list(),
            )
        ).modify(reduced) { exchange ->
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
