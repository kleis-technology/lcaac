package ch.kleis.lcaplugin.core

interface HasUID {
    fun getUID(): String {
        return "${hashCode()}"
    }
}

data class ParameterName(
    val uid: String
) : HasUID {
    override fun getUID(): String {
        return uid
    }
}
