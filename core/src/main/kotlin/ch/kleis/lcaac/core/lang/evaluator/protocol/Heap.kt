package ch.kleis.lcaac.core.lang.evaluator.protocol

class Heap<D> {
    private val data: HashMap<Int, D> = hashMapOf()

    companion object {
        const val VIRTUAL_ADDRESS = -1
    }

    fun store(expression: D): Int {
        val address = expression.hashCode()
        data[address] = expression
        return address
    }

    fun contains(expression: D): Boolean {
        return data.containsKey(expression.hashCode())
    }

    fun modify(address: Int, update: (D) -> D) {
        data[address] = data[address]?.let(update)
            ?: return
    }

    fun free(address: Int) {
        data.remove(address)
    }

    fun pop(address: Int, next: (D) -> Unit) {
        val d = data[address]
        data.remove(address)
        d?.let(next)
    }

    fun popAll(): Collection<D> {
        return data.values
    }
}
