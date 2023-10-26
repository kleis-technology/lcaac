package ch.kleis.lcaac.core.lang.evaluator.reducer

import arrow.optics.Every
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.lang.register.Register
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.Helper
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations

class TemplateExpressionReducer<Q>(
    private val ops: QuantityOperations<Q>,
    dataRegister: DataRegister<Q> = DataRegister.empty(),
) {
    private val dataRegister = Register(dataRegister)
    private val helper = Helper<Q>()

    fun reduce(expression: EProcessTemplateApplication<Q>): EProcess<Q> {
        val template = expression.template

        val unknownParameters = expression.arguments.keys
            .minus(template.params.keys)
        if (unknownParameters.isNotEmpty()) {
            throw EvaluatorException("unknown parameters: $unknownParameters")
        }

        val actualArguments = template.params
            .plus(expression.arguments)

        val localRegister = DataRegister(dataRegister)
            .plus(actualArguments.mapKeys { DataKey(it.key) })
            .plus(template.locals.mapKeys { DataKey(it.key) })

        val reducer = LcaExpressionReducer(localRegister, ops)
        val dataReducer = DataExpressionReducer(localRegister, ops)

        var result = template.body
        actualArguments.forEach {
            result = helper.substitute(it.key, it.value, result)
        }
        result = reducer.reduce(result) as EProcess
        result = concretizeProducts(result, actualArguments, dataReducer)
        return result
    }

    private fun concretizeProducts(
        result: EProcess<Q>,
        actualArguments: Map<String, DataExpression<Q>>,
        dataExpressionReducer: DataExpressionReducer<Q>
    ) = EProcess.products<Q>()
        .compose(Every.list())
        .compose(ETechnoExchange.product())
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
