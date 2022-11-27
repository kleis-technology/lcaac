package ch.kleis.lcaplugin.compute.model

import ch.kleis.lcaplugin.compute.traits.HasUniqueId

data class UnitProcess(
    val name: String,
    val outputs: List<Exchange<*>>,
    val inputs: List<Exchange<*>>,
) : HasUniqueId {
    override fun getUniqueId(): String {
        return name
    }
}

