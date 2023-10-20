package ch.kleis.lcaac.core.lang.evaluator.protocol

data class Address<Q> (
    val connectionIndex: Int, // index for a process or substance characterization
    val portIndex: Int, // index for a product or substance
) {
    companion object {
        fun <Q> virtual(portIndex: Int = 0) = Address<Q>(Heap.VIRTUAL_ADDRESS, portIndex)
    }
}
