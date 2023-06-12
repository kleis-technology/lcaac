package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.language.reservedWords

fun sanitizeSymbol(symbol: String): String {
    return when (symbol) {
        "unit" -> "u"
        in reservedWords -> "_$symbol"
        else -> symbol
    }
}
