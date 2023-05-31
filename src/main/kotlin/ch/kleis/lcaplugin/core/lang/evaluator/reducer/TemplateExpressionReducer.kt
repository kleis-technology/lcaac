package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.Helper
import ch.kleis.lcaplugin.core.lang.expression.*

class TemplateExpressionReducer(
    quantityRegister: Register<QuantityExpression> = Register.empty(),
) : Reducer<ProcessTemplateExpression> {
    private val quantityRegister = Register(quantityRegister)
    private val helper = Helper()

    override fun reduce(expression: ProcessTemplateExpression): ProcessTemplateExpression {
        return when (expression) {
            is EProcessTemplateApplication -> {
                val template = expression.template

                val unknownParameters = expression.arguments.keys
                    .minus(template.params.keys)
                if (unknownParameters.isNotEmpty()) {
                    throw EvaluatorException("unknown parameters: $unknownParameters")
                }

                val actualArguments = template.params
                    .plus(expression.arguments)

                val localRegister = Register(quantityRegister)
                    .plus(actualArguments)
                    .plus(template.locals)

                val reducer = LcaExpressionReducer(localRegister)
                val quantityReducer = QuantityExpressionReducer(localRegister)

                var result = template.body
                actualArguments.forEach {
                    result = helper.substitute(it.key, it.value, result)
                }
                result = reducer.reduce(result) as EProcess
                result = concretizeProducts(result, actualArguments, quantityReducer)
                return EProcessFinal(result)
            }

            is EProcessFinal -> expression
            is EProcessTemplate -> expression
        }
    }

    private fun concretizeProducts(
        result: EProcess,
        actualArguments: Map<String, QuantityExpression>,
        quantityReducer: QuantityExpressionReducer
    ) = EProcess.products
        .compose(Every.list())
        .compose(ETechnoExchange.product)
        .modify(result) { productSpec ->
            val reducedActualArguments = actualArguments.mapValues { quantityReducer.reduce(it.value) }
            productSpec.copy(
                fromProcessRef =
                FromProcessRef(
                    result.name,
                    reducedActualArguments,
                )
            )
        }
}
