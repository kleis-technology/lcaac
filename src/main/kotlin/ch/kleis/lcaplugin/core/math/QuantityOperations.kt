package ch.kleis.lcaplugin.core.math

interface QuantityOperations<Q> {
    operator fun Q.plus(other: Q): Q
    operator fun Q.minus(other: Q): Q
    operator fun Q.times(other: Q): Q
    operator fun Q.div(other: Q): Q
    operator fun Q.unaryPlus(): Q = this
    operator fun Q.unaryMinus(): Q

    fun Q.pow(other: Double): Q

    fun Q.toDouble(): Double
    fun pure(value: Double): Q
}

