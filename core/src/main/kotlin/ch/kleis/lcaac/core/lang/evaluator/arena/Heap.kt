package ch.kleis.lcaac.core.lang.evaluator.arena

class Heap<D> {
    private val data: HashMap<Int, D> = hashMapOf()

    companion object {
        const val VIRTUAL_ADDRESS = -1
    }

    fun add(expression: D): Int {
        val address = (data.keys.maxOrNull() ?: 0) + 1
        data[address] = expression
        return address
    }

    fun modify(address: Int, update: (D) -> D) {
        data[address] = data[address]?.let(update)
            ?: return
    }

    fun find(address: Int): D? {
        return data[address]
    }

    fun remove(address: Int) {
        data.remove(address)
    }

    fun popAll(): Collection<D> {
        val result = data.values.toMutableList()
        data.clear()
        return result
    }
}
