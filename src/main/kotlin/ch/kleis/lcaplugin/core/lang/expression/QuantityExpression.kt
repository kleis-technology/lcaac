package ch.kleis.lcaplugin.core.lang.expression

sealed interface QuantityExpression : Expression
data class EQuantityLiteral(val amount: Double, val unit: UnitExpression) : QuantityExpression
data class EQuantityAdd(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression
data class EQuantitySub(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression
data class EQuantityNeg(val quantity: QuantityExpression): QuantityExpression
data class EQuantityMul(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression
data class EQuantityDiv(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression
data class EQuantityPow(val quantity: QuantityExpression, val exponent: Double) : QuantityExpression
data class EQuantityRef(val name: String): QuantityExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name
}
