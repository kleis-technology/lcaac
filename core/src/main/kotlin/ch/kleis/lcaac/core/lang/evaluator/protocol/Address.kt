package ch.kleis.lcaac.core.lang.evaluator.protocol

sealed interface Address<Q> {
    val connectionIndex: Int
    val portIndex: Int
}

data class PAddr<Q>(override val connectionIndex: Int, override val portIndex: Int) : Address<Q> {
    companion object {
        fun <Q> virtual() = PAddr<Q>(Heap.VIRTUAL_ADDRESS, 0)
    }
}
data class SAddr<Q>(override val connectionIndex: Int, override val portIndex: Int) : Address<Q> {
    companion object {
        fun <Q> virtual() = SAddr<Q>(Heap.VIRTUAL_ADDRESS, 0)
    }
}
