package ch.kleis.lcaac.core.lang.evaluator.protocol

sealed interface Address<Q> {
    val connectionIndex: Int
    val portIndex: Int
}

data class PAddr<Q>(override val connectionIndex: Int, override val portIndex: Int) : Address<Q> {
    companion object {
        fun <Q> virtual(portIndex: Int = 0) = PAddr<Q>(Heap.VIRTUAL_ADDRESS, portIndex)
    }
}
data class SAddr<Q>(override val connectionIndex: Int, override val portIndex: Int) : Address<Q> {
    companion object {
        fun <Q> virtual(portIndex: Int = 0) = SAddr<Q>(Heap.VIRTUAL_ADDRESS, portIndex)
    }
}
