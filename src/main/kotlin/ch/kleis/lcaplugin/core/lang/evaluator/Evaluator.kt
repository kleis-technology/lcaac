package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.*

class Evaluator(
    environment: Environment = Environment.empty(),
) {
    private val processTemplates = environment.processTemplates
    private val reducer = TemplateExpressionReducer(
        environment.products,
        environment.substances,
        environment.indicators,
        environment.quantities,
        environment.units,
        processTemplates,
    )

    fun eval(expression: TemplateExpression): Value {
        val reduced = step(expression)
        return asValue(reduced)
    }
    
    private fun step(expression: TemplateExpression): TemplateExpression {
        val reduced = when (expression) {
            is EInstance -> reducer.reduce(expression)
            is EProcessFinal -> expression
            is EProcessTemplate -> reducer.reduce(EInstance(expression, emptyMap()))
            is ETemplateRef -> processTemplates[expression.name]?.let {
                reducer.reduce(EInstance(expression, emptyMap()))
            } ?: expression
        }
        val unboundedReferences = Helper().unboundedReferences(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return reduced
    }

    private fun asValue(reduced: TemplateExpression): Value {
        return when (reduced) {
            is EProcessFinal -> asValue(reduced.expression)
            else -> throw EvaluatorException("$reduced is not reduced")
        }
    }

    private fun asValue(process: LcaProcessExpression): Value {
        if (process !is EProcess) {
            throw EvaluatorException("$process is not reduced")
        }
        return ProcessValue(
            process.products.map { asValue(it) },
            process.inputs.map { asValue(it) },
            process.biosphere.map { asValue(it) },
        )
    }

    private fun asValue(exchange: ETechnoExchange): TechnoExchangeValue {
        return TechnoExchangeValue(
            asValue(exchange.quantity),
            asValue(exchange.product),
        )
    }

    private fun asValue(exchange: EBioExchange): BioExchangeValue {
        return BioExchangeValue(
            asValue(exchange.quantity),
            asValue(exchange.substance),
        )
    }

    private fun asValue(quantity: QuantityExpression): QuantityValue {
        if (quantity !is EQuantityLiteral) {
            throw EvaluatorException("$quantity is not reduced")
        }
        return QuantityValue(
            quantity.amount,
            asValue(quantity.unit),
        )
    }

    private fun asValue(product: LcaProductExpression): ProductValue {
        return when (product) {
            is EConstrainedProduct -> {
                val actualProduct = product.product
                if (actualProduct !is EProduct) {
                    throw EvaluatorException("$actualProduct is not reduced")
                }
                ProductValue(
                    actualProduct.name,
                    asValue(actualProduct.referenceUnit),
                )
            }

            is EProduct -> ProductValue(
                product.name,
                asValue(product.referenceUnit),
            )

            is EProductRef -> throw EvaluatorException("$product is not reduced")
        }
    }

    private fun asValue(substance: LcaSubstanceExpression): SubstanceValue {
        return when (substance) {
            is ESubstance -> SubstanceValue(
                substance.name,
                substance.compartment,
                substance.subcompartment,
                asValue(substance.referenceUnit),
            )

            is ESubstanceRef -> throw EvaluatorException("$substance is not reduced")
        }
    }

    private fun asValue(unit: UnitExpression): UnitValue {
        return when (unit) {
            is EUnitLiteral -> UnitValue(
                unit.symbol,
                unit.scale,
                unit.dimension,
            )
            else -> throw EvaluatorException("$unit is not reduced")
        }
    }

}
