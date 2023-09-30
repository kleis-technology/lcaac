package ch.kleis.lcaac.core.matrix

import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex

class IndexedPortCollection<Q>(
    elements: Collection<MatrixColumnIndex<Q>>,
) : IndexedCollection<MatrixColumnIndex<Q>>(
   elements
) {
    private val byShortName = HashMap<String, List<Int>>()

    init {
        this.getElements().forEachIndexed { index, port ->
            val shortName = port.getShortName()
            byShortName[shortName] = byShortName[shortName]?.plus(index)
                ?: listOf(index)
        }
    }

    fun findByShortName(shortName: String): List<MatrixColumnIndex<Q>> {
        val indices = byShortName[shortName] ?: return emptyList()
        return indices.map { this[it] }
    }
}
