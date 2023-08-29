package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.ParameterName
import ch.kleis.lcaplugin.core.lang.value.QuantityValue

data class ParameterVector<Q>(
    val names: IndexedCollection<ParameterName>,
    val data: List<QuantityValue<Q>>
) {
    fun size(): Int {
        return names.size()
    }

    fun getName(index: Int): ParameterName {
        return names[index]
    }

    fun getValue(index: Int): QuantityValue<Q> {
        return data[index]
    }

    fun getValue(name: ParameterName): QuantityValue<Q> {
        return data[indexOf(name)]
    }

    fun indexOf(name: ParameterName): Int {
        return names.indexOf(name)
    }
}
