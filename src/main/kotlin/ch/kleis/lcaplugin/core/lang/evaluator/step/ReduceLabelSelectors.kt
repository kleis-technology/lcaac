package ch.kleis.lcaplugin.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.everyDataRefInDataExpression

class ReduceLabelSelectors(
    private val symbolTable: SymbolTable
) {
    private val everyInputProduct =
        EProcessTemplateApplication.template.body.inputs compose
            Every.list() compose
            ETechnoExchange.product
    private val everyLabelSelector = everyInputProduct compose
        EProductSpec.fromProcess.matchLabels.elements compose
        Every.map() compose
        everyDataRefInDataExpression

    fun apply(expression: EProcessTemplateApplication): EProcessTemplateApplication {
        val template = expression.template
        val labels = template.body.labels
        val actualArguments = template.params.plus(expression.arguments)
        val locals = template.locals
        val reducer = DataExpressionReducer(
            Register(symbolTable.data)
                .plus(actualArguments)
                .plus(labels)
                .plus(locals)
        )
        return everyLabelSelector.modify(expression) { ref -> reducer.reduce(ref) }
    }
}
