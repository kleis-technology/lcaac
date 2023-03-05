package ch.kleis.lcaplugin.core.lang_obsolete

data class EntryPoint(
    val pkg: Package,
    val symbol: String,
) {
    fun fqn(): String {
        return "${pkg.name}.$symbol"
    }
}
