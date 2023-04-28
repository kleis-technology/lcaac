package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.Helper
import ch.kleis.lcaplugin.core.lang.expression.*

class TemplateExpressionReducer(
    substanceRegister: Register<ESubstance> = Register.empty(),
    indicatorRegister: Register<EIndicator> = Register.empty(),
    quantityRegister: Register<QuantityExpression> = Register.empty(),
    unitRegister: Register<UnitExpression> = Register.empty(),
    templateRegister: Register<EProcessTemplate> = Register.empty(),
) : Reducer<ProcessTemplateExpression> {
    private val templateRegister = Register(templateRegister)
    private val substanceRegister = Register(substanceRegister)
    private val indicatorRegister = Register(indicatorRegister)
    private val quantityRegister = Register(quantityRegister)
    private val unitRegister = Register(unitRegister)
    private val helper = Helper()

    override fun reduce(expression: ProcessTemplateExpression): ProcessTemplateExpression {
        return when (expression) {
            is EProcessTemplateApplication -> {
                val template = reduce(expression.template) as EProcessTemplate

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

                val reducer = LcaExpressionReducer(
                    substanceRegister,
                    indicatorRegister,
                    localRegister,
                    unitRegister
                )
                val quantityReducer = QuantityExpressionReducer(
                    localRegister, unitRegister,
                )

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
            is EProcessTemplateRef -> templateRegister[expression.name]?.let { reduce(it) } ?: expression
        }
    }

    private fun concretizeProducts(
        result: EProcess,
        actualArguments: Map<String, QuantityExpression>,
        quantityReducer: QuantityExpressionReducer
    ) = (EProcess.products
        .compose(Every.list())
        .compose(ETechnoExchange.product)).modify(result) {
            val reducedActualArguments = actualArguments.mapValues { quantityReducer.reduce(it.value) }
            it.withFromProcessRef(
                FromProcessRef(
                    result.name,
                    reducedActualArguments,
                )
            )
        }
}
