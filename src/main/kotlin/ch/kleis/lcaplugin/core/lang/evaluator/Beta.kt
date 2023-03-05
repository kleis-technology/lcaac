package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*

class Beta {
    fun substitute(binder: String, value: QuantityExpression, body: LcaProcessExpression): LcaProcessExpression {
        return when (body) {
            is EProcess -> EProcess(
                products = body.products.map { substitute(binder, value, it) },
                inputs = body.inputs.map { substitute(binder, value, it) },
                biosphere = body.biosphere.map { substitute(binder, value, it) },
            )

            is EProcessRef -> body
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
        }
    }
}

class Helper {
    fun newName(binder: String, others: Set<String>): String {
        var i = 0
        var result = "${binder}0"
        while (others.contains(result)) {
            i += 1
            result = "$binder$i"
        }
        return result
    }
}
