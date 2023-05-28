package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.EStringLiteral
import ch.kleis.lcaplugin.core.lang.expression.EStringRef
import ch.kleis.lcaplugin.core.lang.expression.StringExpression

class StringExpressionReducer(
    private val stringRegister: Register<StringExpression>,
) : Reducer<StringExpression> {
    override fun reduce(expression: StringExpression): StringExpression {
        return when (expression) {
            is EStringLiteral -> expression
            is EStringRef -> reduceRef(expression)
        }
    }

    private fun reduceRef(expression: EStringRef): StringExpression {
        return stringRegister[expression.name]?.let { reduce(it) } ?: expression
    }
}
