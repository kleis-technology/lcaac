package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.Helper
import ch.kleis.lcaplugin.core.lang.expression.*

class TemplateExpressionReducer(
    dataRegister: Register<DataExpression> = Register.empty(),
) : Reducer<ProcessTemplateExpression> {
    private val dataRegister = Register(dataRegister)
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

                val localRegister = Register(dataRegister)
                    .plus(actualArguments)
                    .plus(template.locals)

                val reducer = LcaExpressionReducer(localRegister)
                val dataReducer = DataExpressionReducer(localRegister)

                var result = template.body
                actualArguments.forEach {
                    result = helper.substitute(it.key, it.value, result)
                }
                result = reducer.reduce(result) as EProcess
                result = concretizeProducts(result, actualArguments, dataReducer)
                return EProcessFinal(result)
            }

            is EProcessFinal -> expression
            is EProcessTemplate -> expression
        }
    }

    private fun concretizeProducts(
        result: EProcess,
        actualArguments: Map<String, DataExpression>,
        dataExpressionReducer: DataExpressionReducer
    ) = EProcess.products
        .compose(Every.list())
        .compose(ETechnoExchange.product)
        .modify(result) { productSpec ->
            val reducedActualArguments = actualArguments.mapValues { dataExpressionReducer.reduce(it.value) }
            productSpec.copy(
                fromProcess =
                FromProcess(
                    result.name,
                    MatchLabels(result.labels),
                    reducedActualArguments,
                )
            )
        }
}
