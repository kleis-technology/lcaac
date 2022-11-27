package ch.kleis.lcaplugin.lib.model

import ch.kleis.lcaplugin.lib.urn.URN
import ch.kleis.lcaplugin.lib.traits.HasUrn

data class UnitProcess(
    private val urn: URN,
    val outputs: List<Exchange<*>>,
    val inputs: List<Exchange<*>>,
) : HasUrn {
    override fun getUrn(): URN {
        return urn
    }
}

