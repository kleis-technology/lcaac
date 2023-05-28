package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.DataExpression
import ch.kleis.lcaplugin.core.lang.expression.QuantityExpression
import ch.kleis.lcaplugin.core.lang.expression.StringExpression

class DataExpressionReducer(
    register: Register<DataExpression> = Register.empty(),
) : Reducer<DataExpression> {
    private val quantityReducer = QuantityExpressionReducer(
        register.filterMap { it as? QuantityExpression }
    )
    private val stringReducer = StringExpressionReducer(
        register.filterMap { it as? StringExpression }
    )

    override fun reduce(expression: DataExpression): DataExpression {
        return when (expression) {
            is QuantityExpression -> reduceQuantity(expression)
            is StringExpression -> reduceString(expression)
        }
    }

    fun reduceQuantity(expression: QuantityExpression): QuantityExpression = quantityReducer.reduce(expression)
    fun reduceString(expression: StringExpression): StringExpression = stringReducer.reduce(expression)
}
