package ch.kleis.lcaplugin.core.lang

data class EntryPoint(
    val pkg: Package,
    val symbol: String,
) {
    fun getExpression(): Expression {
        return pkg.findByLocalName(symbol)
            ?: throw NoSuchElementException("cannot find $symbol in ${pkg.name}")
    }
}
