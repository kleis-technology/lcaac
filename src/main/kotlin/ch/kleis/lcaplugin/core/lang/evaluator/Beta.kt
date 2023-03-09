package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.FromProcessRef
import ch.kleis.lcaplugin.core.lang.None
import ch.kleis.lcaplugin.core.lang.expression.*

class Beta {
    fun substitute(binder: String, value: QuantityExpression, body: LcaProcessExpression): LcaProcessExpression {
        return when (body) {
            is EProcess -> EProcess(
                products = body.products.map { substitute(binder, value, it) },
                inputs = body.inputs.map { substitute(binder, value, it) },
                biosphere = body.biosphere.map { substitute(binder, value, it) },
            )
        }
    }

    private fun substitute(binder: String, value: QuantityExpression, exchange: ETechnoExchange): ETechnoExchange {
        return ETechnoExchange(
            substitute(binder, value, exchange.quantity),
            substitute(binder, value, exchange.product),
        )
    }

    private fun substitute(binder: String, value: QuantityExpression, exchange: EBioExchange): EBioExchange {
        return EBioExchange(
            substitute(binder, value, exchange.quantity),
            exchange.substance,
        )
    }

    private fun substitute(
        binder: String,
        value: QuantityExpression,
        product: LcaProductExpression
    ): LcaProductExpression {
        return when (product) {
            is EConstrainedProduct -> EConstrainedProduct(
                product.product,
                product.constraint.substituteWith(this, binder, value),
            )

            is EProduct -> product
            is EProductRef -> product
        }
    }

    fun substitute(
        binder: String,
        value: QuantityExpression,
        quantity: QuantityExpression
    ): QuantityExpression {
        return when (quantity) {
            is EQuantityAdd -> EQuantityAdd(
                substitute(binder, value, quantity.left),
                substitute(binder, value, quantity.right),
            )

            is EQuantityDiv -> EQuantityDiv(
                substitute(binder, value, quantity.left),
                substitute(binder, value, quantity.right),
            )

            is EQuantityLiteral -> quantity
            is EQuantityMul -> EQuantityMul(
                substitute(binder, value, quantity.left),
                substitute(binder, value, quantity.right),
            )

            is EQuantityPow -> EQuantityPow(
                substitute(binder, value, quantity.quantity),
                quantity.exponent,
            )

            is EQuantityRef -> if (binder == quantity.name) value else quantity
            is EQuantitySub -> EQuantitySub(
                substitute(binder, value, quantity.left),
                substitute(binder, value, quantity.right),
            )

            is EQuantityNeg -> EQuantityNeg(
                substitute(binder, value, quantity.quantity)
            )
        }
    }
}

class Helper {
    fun unboundedReferences(expression: Expression): Set<RefExpression> {
        return when (expression) {
            is EBioExchange -> unboundedReferences(expression.quantity)
                .plus(unboundedReferences(expression.substance))

            is EImpact -> unboundedReferences(expression.quantity)
                .plus(unboundedReferences(expression.indicator))

            is ETechnoExchange -> unboundedReferences(expression.quantity)
                .plus(unboundedReferences(expression.product))

            is EIndicator -> unboundedReferences(expression.referenceUnit)
            is EIndicatorRef -> setOf(expression)
            is EProcess -> expression.products.flatMap { unboundedReferences(it) }
                .plus(expression.inputs.flatMap { unboundedReferences(it) })
                .plus(expression.biosphere.flatMap { unboundedReferences(it) })
                .toSet()

            is EConstrainedProduct -> when (expression.constraint) {
                is FromProcessRef -> unboundedReferences(expression.product)
                    .plus(expression.constraint.arguments.flatMap { unboundedReferences(it.value) })

                None -> unboundedReferences(expression.product)
            }

            is EProduct -> unboundedReferences(expression.referenceUnit)
            is EProductRef -> setOf(expression)
            is ESubstanceCharacterization -> unboundedReferences(expression.referenceExchange)
                .plus(expression.impacts.flatMap { unboundedReferences(it) })

            is ESubstance -> unboundedReferences(expression.referenceUnit)
            is ESubstanceRef -> setOf(expression)
            is EQuantityAdd -> unboundedReferences(expression.left)
                .plus(unboundedReferences(expression.right))

            is EQuantityDiv -> unboundedReferences(expression.left)
                .plus(unboundedReferences(expression.right))

            is EQuantityLiteral -> unboundedReferences(expression.unit)
            is EQuantityMul -> unboundedReferences(expression.left)
                .plus(unboundedReferences(expression.right))

            is EQuantityPow -> unboundedReferences(expression.quantity)
            is EQuantityRef -> setOf(expression)
            is EQuantitySub -> unboundedReferences(expression.left)
                .plus(unboundedReferences(expression.right))

            is EUnitDiv -> unboundedReferences(expression.left)
                .plus(unboundedReferences(expression.right))

            is EUnitLiteral -> emptySet()
            is EUnitMul -> unboundedReferences(expression.left)
                .plus(unboundedReferences(expression.right))

            is EUnitPow -> unboundedReferences(expression.unit)
            is EUnitRef -> setOf(expression)

            is EInstance -> unboundedReferences(expression.template)
                .minus(expression.arguments.keys.map { EQuantityRef(it) }.toSet())

            is EProcessFinal -> unboundedReferences(expression.expression)
            is EProcessTemplate -> unboundedReferences(expression.body)
                .minus(expression.params.keys.map { EQuantityRef(it) }.toSet())
                .minus(expression.locals.keys.map { EQuantityRef(it) }.toSet())

            is ETemplateRef -> setOf(expression)
            is EQuantityNeg -> unboundedReferences(expression.quantity)
            is ESystem -> expression.processes.flatMap { unboundedReferences(it) }.toSet()
            is ESubstanceCharacterizationRef -> setOf(expression)
        }
    }
}
