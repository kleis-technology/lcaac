package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.Helper
import ch.kleis.lcaplugin.core.lang.expression.*

class TemplateExpressionReducer(
    productRegister: Register<LcaUnconstrainedProductExpression> = Register.empty(),
    substanceRegister: Register<LcaSubstanceExpression> = Register.empty(),
    indicatorRegister: Register<LcaIndicatorExpression> = Register.empty(),
    quantityRegister: Register<QuantityExpression> = Register.empty(),
    unitRegister: Register<UnitExpression> = Register.empty(),
    templateRegister: Register<ProcessTemplateExpression> = Register.empty(),
) : Reducer<ProcessTemplateExpression> {
    private val templateRegister = Register(templateRegister)
    private val productRegister = Register(productRegister)
    private val substanceRegister = Register(substanceRegister)
    private val indicatorRegister = Register(indicatorRegister)
    private val quantityRegister = Register(quantityRegister)
    private val unitRegister = Register(unitRegister)
    private val helper = Helper()
    private val everyConstraintInProducts = LcaProcessExpression.eProcess.products compose
            Every.list() compose
            ETechnoExchange.product.eConstrainedProduct.constraint

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
                    productRegister,
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
                result = constrainProducts(result as EProcess, actualArguments, quantityReducer)
                return EProcessFinal(result)
            }

            is EProcessFinal -> expression
            is EProcessTemplate -> expression
            is EProcessTemplateRef -> templateRegister[expression.name]?.let { reduce(it) } ?: expression
        }
    }

    private fun constrainProducts(
        result: EProcess,
        actualArguments: Map<String, QuantityExpression>,
        quantityReducer: QuantityExpressionReducer
    ) = everyConstraintInProducts.modify(result) {
        val reducedActualArguments = actualArguments.mapValues { quantityReducer.reduce(it.value) }
        FromProcessRef(
            result.name,
            reducedActualArguments,
        )
    }
}
