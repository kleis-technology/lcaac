package ch.kleis.lcaac.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.Register
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.expression.optics.everyDataRefInDataExpression
import ch.kleis.lcaac.core.math.QuantityOperations

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
        Every.map() compose
        everyDataRefInDataExpression()

    fun apply(expression: EProcessTemplateApplication<Q>): EProcessTemplateApplication<Q> {
        val template = expression.template
        val labels = template.body.labels
        val actualArguments = template.params.plus(expression.arguments)
        val locals = template.locals
        val reducer = DataExpressionReducer(
            Register(symbolTable.data)
                .plus(actualArguments.mapKeys { DataKey(it.key) })
                .plus(labels.mapKeys { DataKey(it.key) })
                .plus(locals.mapKeys { DataKey(it.key) }),
            ops,
        )
        return everyLabelSelector.modify(expression) { ref -> reducer.reduce(ref) }
    }
}
