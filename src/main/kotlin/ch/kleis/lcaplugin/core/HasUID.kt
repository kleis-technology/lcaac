package ch.kleis.lcaplugin.core

interface HasUID {
    fun getUID(): String {
        return "${hashCode()}"
    }
}
