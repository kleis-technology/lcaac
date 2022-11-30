package ch.kleis.lcaplugin.compute.traits

import ch.kleis.lcaplugin.compute.urn.URN

interface HasUrn : HasUniqueId {
    fun getUrn(): URN
    override fun getUniqueId(): String {
        return getUrn().uid
    }
}
