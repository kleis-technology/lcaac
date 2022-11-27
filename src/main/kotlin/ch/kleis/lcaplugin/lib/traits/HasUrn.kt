package ch.kleis.lcaplugin.lib.traits

import ch.kleis.lcaplugin.lib.urn.URN

interface HasUrn : HasUniqueId {
    fun getUrn(): URN
    override fun getUniqueId(): String {
        return getUrn().uid
    }
}
