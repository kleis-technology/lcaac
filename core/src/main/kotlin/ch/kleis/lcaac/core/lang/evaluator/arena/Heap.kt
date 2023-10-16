package ch.kleis.lcaac.core.lang.evaluator.arena

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

    fun popAll(): Collection<D> {
        val result = data.values.toMutableList()
        data.clear()
        return result
    }
}
