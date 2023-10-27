package ch.kleis.lcaac.core.lang.register

import ch.kleis.lcaac.core.lang.dimension.Dimension

data class DimensionKey(
    val name: String,
){
    override fun toString() = name
}
typealias DimensionRegister = Register<DimensionKey, Dimension>
