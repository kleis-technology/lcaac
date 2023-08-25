package ch.kleis.lcaplugin.core.lang.evaluator.step

import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.Helper
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.LcaExpressionReducer
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.TemplateExpressionReducer
import ch.kleis.lcaplugin.core.lang.expression.EProcessFinal
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaplugin.core.math.QuantityOperations

class Reduce<Q>(
    symbolTable: SymbolTable<Q>,
    ops: QuantityOperations<Q>,
) {
    private val lcaReducer = LcaExpressionReducer(
        symbolTable.data,
        ops
    )
    private val templateReducer = TemplateExpressionReducer(
        symbolTable.data,
        ops,
    )

    fun apply(expression: EProcessTemplateApplication<Q>): EProcessFinal<Q> {
        val reduced = templateReducer.reduceTemplateApplication(expression)
        val unboundedReferences = Helper<Q>().allRequiredRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return reduced
    }

    fun apply(expression: ESubstanceCharacterization<Q>): ESubstanceCharacterization<Q> {
        val reduced = lcaReducer.reduceSubstanceCharacterization(expression)
        val unboundedReferences = Helper<Q>().allRequiredRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return reduced
    }


}
