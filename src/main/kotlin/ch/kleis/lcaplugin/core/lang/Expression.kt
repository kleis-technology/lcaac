package ch.kleis.lcaplugin.core.lang

import kotlin.math.pow

sealed interface Expression

/*
    Quantities
 */

data class EUnit(val symbol: String, val scale: Double, val dimension: Dimension) : Expression {
    constructor(symbol: String, scale: Double, dimension: String) : this(symbol, scale, Dimension.of(dimension))

    fun multiply(other: EUnit): EUnit {
        return EUnit(
            "${symbol}.${other.symbol}",
            scale * other.scale,
            dimension.multiply(other.dimension),
        )
    }

    fun divide(other: EUnit): EUnit {
        return EUnit(
            "${symbol}/${other.symbol}",
            scale / other.scale,
            dimension.divide(other.dimension),
        )
    }

    fun pow(n: Double): EUnit {
        return EUnit(
            "${symbol}^(${n})",
            scale.pow(n),
            dimension.pow(n),
        )
    }
}

data class EQuantity(val amount: Double, val unit: Expression) : Expression

data class ENeg(val quantity: Expression) : Expression

data class EMul(val left: Expression, val right: Expression) : Expression

data class EDiv(val left: Expression, val right: Expression) : Expression

data class EAdd(val left: Expression, val right: Expression) : Expression

data class ESub(val left: Expression, val right: Expression) : Expression

data class EPow(val quantity: Expression, val exponent: Double) : Expression


/*
    LCA Modeling
 */

data class EProduct(
    val name: String,
    val dimension: Dimension,
    val referenceUnit: Expression,
) : Expression

data class EProcess(
    val elements: List<Expression>
) : Expression

data class ESystem(
    val elements: List<Expression>
) : Expression

enum class Polarity {
    POSITIVE, NEGATIVE
}

data class EBlock(
    val elements: List<Expression>,
    val polarity: Polarity = Polarity.POSITIVE
) : Expression

data class EExchange(
    val quantity: Expression,
    val product: Expression,
) : Expression

/*
    Lambda calculus
 */

data class EVar(val name: String) : Expression
data class ELet(val locals: Map<String, Expression>, val body: Expression) : Expression
data class ETemplate(val params: Map<String, Expression?>, val body: Expression) : Expression
data class EInstance(val template: Expression, val arguments: Map<String, Expression>) : Expression
