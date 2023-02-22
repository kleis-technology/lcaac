package ch.kleis.lcaplugin.core.lang

data class EntryPoint(
    val pkg: Package,
    val symbol: String,
) {
    fun fqn(): String {
        return "${pkg.name}.$symbol"
    }
}
