package ch.kleis.lcaac.core.lang.register

import ch.kleis.lcaac.core.lang.expression.EDataSource

data class DataSourceKey(
        val name: String,
) {
    override fun toString(): String = name
}

typealias DataSourceRegister<Q> = Register<DataSourceKey, EDataSource<Q>>

