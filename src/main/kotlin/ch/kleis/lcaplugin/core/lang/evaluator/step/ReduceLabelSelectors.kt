package ch.kleis.lcaplugin.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.everyDataRefInDataExpression
import ch.kleis.lcaplugin.core.math.QuantityOperations

class ReduceLabelSelectors<Q>(
    private val symbolTable: SymbolTable<Q>,
    private val ops: QuantityOperations<Q>,
) {
    private val everyInputProduct =
        EProcessTemplateApplication.template<Q>().body().inputs() compose
            Every.list() compose
            ETechnoExchange.product()
    private val everyLabelSelector = everyInputProduct compose
        EProductSpec.fromProcess<Q>().matchLabels().elements() compose
        Every.map<String, DataExpression<Q>>() compose
        everyDataRefInDataExpression()

    fun apply(expression: EProcessTemplateApplication<Q>): EProcessTemplateApplication<Q> {
        val template = expression.template
        val labels = template.body.labels
        val actualArguments = template.params.plus(expression.arguments)
        val locals = template.locals
        val reducer = DataExpressionReducer(
            Register(symbolTable.data)
                .plus(actualArguments)
                .plus(labels)
                .plus(locals),
            ops,
        )
        return everyLabelSelector.modify(expression) { ref -> reducer.reduce(ref) }
    }
}
