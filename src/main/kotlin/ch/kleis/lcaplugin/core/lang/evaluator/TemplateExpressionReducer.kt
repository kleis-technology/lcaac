package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*

class TemplateExpressionReducer(
    processEnvironment: Environment<LcaProcessExpression> = Environment.empty(),
    productEnvironment: Environment<LcaUnconstrainedProductExpression> = Environment.empty(),
    substanceEnvironment: Environment<LcaSubstanceExpression> = Environment.empty(),
    indicatorEnvironment: Environment<LcaIndicatorExpression> = Environment.empty(),
    quantityEnvironment: Environment<QuantityExpression> = Environment.empty(),
    unitEnvironment: Environment<UnitExpression> = Environment.empty(),
    templateEnvironment: Environment<TemplateExpression> = Environment.empty(),
) : Reducer<TemplateExpression> {
    private val templateEnvironment = Environment(templateEnvironment)
    private val processEnvironment = Environment(processEnvironment)
    private val productEnvironment = Environment(productEnvironment)
    private val substanceEnvironment = Environment(substanceEnvironment)
    private val indicatorEnvironment = Environment(indicatorEnvironment)
    private val quantityEnvironment = Environment(quantityEnvironment)
    private val unitEnvironment = Environment(unitEnvironment)
    private val beta = Beta()

    override fun reduce(expression: TemplateExpression): TemplateExpression {
        return when (expression) {
            is EInstance -> {
                val template = reduce(expression.template) as EProcessTemplate

                val unknownParameters = expression.arguments.keys
                    .minus(template.params.keys)
                if (unknownParameters.isNotEmpty()) {
                    throw EvaluatorException("unknown parameters: $unknownParameters")
                }

                val localEnvironment = Environment(quantityEnvironment)

                val actualArguments = template.params
                    .plus(expression.arguments)
                actualArguments.forEach {
                    localEnvironment[it.key] = it.value
                }

                template.locals.forEach {
                    localEnvironment[it.key] = it.value
                }

                val reducer = LcaExpressionReducer(
                    processEnvironment,
                    productEnvironment,
                    substanceEnvironment,
                    indicatorEnvironment,
                    localEnvironment,
                    unitEnvironment
                )

                var result = template.body
                actualArguments.forEach {
                    result = beta.substitute(it.key, it.value, result)
                }
                return EProcessFinal(
                    reducer.reduce(result) as LcaProcessExpression
                )
            }

            is EProcessFinal -> expression
            is EProcessTemplate -> expression
            is ETemplateRef -> templateEnvironment[expression.name]?.let { reduce(it) } ?: expression
        }
    }
}
