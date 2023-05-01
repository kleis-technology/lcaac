package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.language.reservedWords

fun sanitizeUnit(symbol: String): String {
    return if (symbol in reservedWords) {
        "_$symbol"
    } else {
        symbol
    }
}
