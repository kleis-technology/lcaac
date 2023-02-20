package ch.kleis.lcaplugin.core.lang

sealed interface Type

data class TProduct(val name: String, val dim: Dimension, val constraint: Constraint) : Type

data class TSystem(
    val reference: TProduct,
    val coproducts: List<TProduct>,
    val inputs: List<TProduct>,
)

data class TVar(val name: String): Type


sealed interface Constraint

object None : Constraint
