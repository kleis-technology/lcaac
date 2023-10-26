package ch.kleis.lcaac.core.lang.register

import ch.kleis.lcaac.core.lang.expression.DataExpression

data class DataKey(
    val name: String,
) {
    override fun toString() = name
}
typealias DataRegister<Q> = Register<DataKey, DataExpression<Q>>
