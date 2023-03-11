package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics

@optics
sealed interface QuantityExpression : Expression {
    companion object
}

@optics
data class EQuantityLiteral(val amount: Double, val unit: UnitExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityAdd(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantitySub(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityNeg(val quantity: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityMul(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityDiv(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityPow(val quantity: QuantityExpression, val exponent: Double) : QuantityExpression {
    companion object
}

@optics
data class EQuantityRef(val name: String) : QuantityExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name

    companion object
}

