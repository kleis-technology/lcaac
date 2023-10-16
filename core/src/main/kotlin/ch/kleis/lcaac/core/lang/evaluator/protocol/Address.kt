package ch.kleis.lcaac.core.lang.evaluator.protocol

data class Address<Q> (
    val connectionIndex: Int,
    val portIndex: Int,
) {
    companion object {
        fun <Q> virtual(portIndex: Int = 0) = Address<Q>(Heap.VIRTUAL_ADDRESS, portIndex)
    }
}
