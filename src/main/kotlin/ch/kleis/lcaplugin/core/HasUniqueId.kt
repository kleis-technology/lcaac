package ch.kleis.lcaplugin.core

interface HasUniqueId {
    fun getUniqueId(): String {
        return "${hashCode()}"
    }
}
