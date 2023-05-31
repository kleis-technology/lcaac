package ch.kleis.lcaplugin.language.psi.type.spec

import ch.kleis.lcaplugin.language.psi.reference.OutputProductReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner

interface PsiInputProductSpec : PsiUIDOwner {
    override fun getReference(): OutputProductReference {
        return OutputProductReference(this)
    }
}
