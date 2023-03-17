package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.trait.PsiUrnOwner

interface PsiImport : PsiUrnOwner {
    fun getPackageName(): String {
        val parts = getUrn().getParts()
        return parts.joinToString(".")
    }
}
