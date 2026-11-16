package ch.kleis.lcaac.core.lang.evaluator.protocol

class Heap<D> {
    private val data: HashMap<Int, D> = hashMapOf()
    private var counter: Int = 0

    companion object {
        const val VIRTUAL_ADDRESS = -1
    }

    fun add(expression: D): Int {
        val index = counter + 1
        counter = index
        data[index] = expression
        return index
    }

    fun modify(index: Int, update: (D) -> D) {
        data[index] = data[index]?.let(update)
            ?: return
    }

    fun find(index: Int): D? {
        return data[index]
    }

    fun remove(index: Int) {
        data.remove(index)
    }

    /** Removes and returns all the elements with their index. */
    fun popAllIndexed(): Map<Int, D> {
        val result = HashMap(data)
        data.clear()
        return result
    }

    fun popAll(): Collection<D> {
        val result = data.values.toMutableList()
        data.clear()
        return result
    }
}
