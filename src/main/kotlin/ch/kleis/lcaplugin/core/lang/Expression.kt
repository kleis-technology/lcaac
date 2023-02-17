package ch.kleis.lcaplugin.core.lang

sealed interface Expression

/*
    Quantities
 */

data class EUnit(val symbol: String ,val scale: Double, val dimension: Dimension) : Expression
data class EQuantity(val amount: Double, val unit: Expression) : Expression
data class EQNeg(val quantity: Expression) : Expression
data class EQMul(val left: Expression, val right: Expression) : Expression
data class EQDiv(val left: Expression, val right: Expression) : Expression
data class EQAdd(val left: Expression, val right: Expression) : Expression
data class EQSub(val left: Expression, val right: Expression) : Expression
data class EQPow(val quantity: Expression, val exponent: Exponent) : Expression



/*
    LCA Modeling
 */

data class EProduct(val name: String, val dimension: Dimension, val referenceUnit: Expression) : Expression
data class EExchange(val quantity: Expression, val product: Expression) : Expression
data class EProcess(
    val elements: List<Expression>
) : Expression

data class ESystem(
    val processes: List<Expression>
) : Expression

/*
    Lambda calculus
 */

data class EVar(val name: String) : Expression
