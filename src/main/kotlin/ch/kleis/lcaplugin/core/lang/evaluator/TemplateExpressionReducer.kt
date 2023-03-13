package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.*

class TemplateExpressionReducer(
    productRegister: Register<LcaUnconstrainedProductExpression> = Register.empty(),
    substanceRegister: Register<LcaSubstanceExpression> = Register.empty(),
    indicatorRegister: Register<LcaIndicatorExpression> = Register.empty(),
    quantityRegister: Register<QuantityExpression> = Register.empty(),
    unitRegister: Register<UnitExpression> = Register.empty(),
    templateRegister: Register<TemplateExpression> = Register.empty(),
) : Reducer<TemplateExpression> {
    private val templateRegister = Register(templateRegister)
    private val productRegister = Register(productRegister)
    private val substanceRegister = Register(substanceRegister)
    private val indicatorRegister = Register(indicatorRegister)
    private val quantityRegister = Register(quantityRegister)
    private val unitRegister = Register(unitRegister)
    private val helper = Helper()

    override fun reduce(expression: TemplateExpression): TemplateExpression {
        return when (expression) {
            is EInstance -> {
                val template = reduce(expression.template) as EProcessTemplate

                val unknownParameters = expression.arguments.keys
                    .minus(template.params.keys)
                if (unknownParameters.isNotEmpty()) {
                    throw EvaluatorException("unknown parameters: $unknownParameters")
                }

                val localRegister = Register(quantityRegister)

                val actualArguments = template.params
                    .plus(expression.arguments)
                actualArguments.forEach {
                    localRegister[it.key] = it.value
                }

                template.locals.forEach {
                    localRegister[it.key] = it.value
                }

                val reducer = LcaExpressionReducer(
                    productRegister,
                    substanceRegister,
                    indicatorRegister,
                    localRegister,
                    unitRegister
                )

                var result = template.body
                actualArguments.forEach {
                    result = helper.substitute(it.key, it.value, result)
                }
                return EProcessFinal(
                    reducer.reduce(result) as LcaProcessExpression
                )
            }

            is EProcessFinal -> expression
            is EProcessTemplate -> expression
            is ETemplateRef -> templateRegister[expression.name]?.let { reduce(it) } ?: expression
        }
    }
}
