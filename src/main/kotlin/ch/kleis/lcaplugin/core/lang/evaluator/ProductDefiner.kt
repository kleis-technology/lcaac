package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*

class ProductDefiner {
    fun complete(expression: Expression): Expression {
        return when(expression) {
            is EAdd -> expression
            is EBlock -> EBlock(
                expression.elements.map { complete(it) }
            )
            is EExchange -> {
                val quantity = expression.quantity as EQuantity
                val unit = quantity.unit as EUnit
                val product = expression.product
                if (product is EVar) {
                    return EExchange(
                        quantity,
                        EProduct(
                            product.name,
                            unit,
                        )
                    )
                }
                return expression
            }
            is EInstance -> throw IllegalStateException("ProductDefiner should not receive an instance expression")
            is ETemplate -> throw IllegalStateException("ProductDefiner should not receive a template expression")
            is ELet -> ELet(
                expression.locals,
                complete(expression.body),
            )
            is EMul -> expression
            is ENeg -> expression
            is EPow -> expression
            is EDiv -> expression
            is EProcess -> EProcess(
                expression.elements.map { complete(it) }
            )
            is EProduct -> expression
            is EQuantity -> expression
            is ESub -> expression
            is ESystem -> ESystem(
                expression.elements.map { complete(it) }
            )
            is EUnit -> expression
            is EVar -> expression
        }
    }
}
