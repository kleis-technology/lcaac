package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.OutputProductReferenceFromPsiProductRef
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner

interface PsiProductRef : PsiUIDOwner {
    override fun getReference(): OutputProductReferenceFromPsiProductRef {
        return OutputProductReferenceFromPsiProductRef(this)
    }
}
