package ch.kleis.lcaplugin.compute.model

import ch.kleis.lcaplugin.compute.urn.URN
import ch.kleis.lcaplugin.compute.traits.HasUrn

data class UnitProcess(
    private val urn: URN,
    val outputs: List<Exchange<*>>,
    val inputs: List<Exchange<*>>,
) : HasUrn {
    override fun getUrn(): URN {
        return urn
    }
}

