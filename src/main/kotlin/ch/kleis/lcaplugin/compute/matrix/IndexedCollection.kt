package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.HasUniqueId
import java.util.stream.IntStream.range

class IndexedCollection<V : HasUniqueId>(elements: Collection<V>) {
    private val byEntity = HashMap<V, Int>()
    private val byUniqueId = HashMap<String, Int>()
    private val elements = ArrayList<V>()

    init {
        val unique = elements.stream()
            .distinct()
            .toList()
        range(0, unique.size).forEach { i ->
            val element = unique[i]
            this.byEntity[element] = i
            this.byUniqueId[element.getUniqueId()] = i
            this.elements[i] = element
        }
    }

    fun getElements(): List<V> {
        return elements
    }

    fun indexOf(element: V): Int {
        return byEntity[element] ?: throw NoSuchElementException(element.toString())
    }

    fun indexOf(id: String): Int {
        return byUniqueId[id] ?: throw NoSuchElementException(id)
    }

    fun get(i: Int): V {
        return elements[i]
    }

    fun get(id: String): V {
        return elements[indexOf(id)]
    }

    fun size(): Int {
        return elements.size
    }

    fun contains(element: V): Boolean {
        return elements.contains(element)
    }
}
