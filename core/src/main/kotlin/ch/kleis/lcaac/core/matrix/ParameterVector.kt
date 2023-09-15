package ch.kleis.lcaac.core.matrix

import ch.kleis.lcaac.core.ParameterName
import ch.kleis.lcaac.core.lang.value.QuantityValue

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
