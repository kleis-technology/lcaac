package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.trait.PsiUrnOwner
import ch.kleis.lcaplugin.psi.LcaTypes

enum class ImportType {
    SYMBOL, WILDCARD
}
interface PsiImport : PsiUrnOwner {
    fun getImportType(): ImportType {
        return node.findChildByType(LcaTypes.WILDCARD)?.let { ImportType.WILDCARD }
            ?: ImportType.SYMBOL
    }

    fun getPackageName(): String {
        val parts = getUrn().getParts()
        return when(getImportType()) {
            ImportType.SYMBOL -> parts.take(parts.size - 1).joinToString(".")
            ImportType.WILDCARD -> parts.joinToString(".")
        }
    }

    fun getSymbol(): String? {
        val parts = getUrn().getParts()
        return when(getImportType()) {
            ImportType.SYMBOL -> parts.last()
            ImportType.WILDCARD -> null
        }
    }
}
