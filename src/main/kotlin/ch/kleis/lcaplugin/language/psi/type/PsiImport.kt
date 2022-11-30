package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.traits.PsiUrnOwner
import ch.kleis.lcaplugin.psi.LcaTypes

interface PsiImport : PsiUrnOwner {
    fun isWildcard(): Boolean {
        return node.findChildByType(LcaTypes.WILDCARD) != null
    }

    fun isNotWildcard(): Boolean {
        return !isWildcard()
    }
}
