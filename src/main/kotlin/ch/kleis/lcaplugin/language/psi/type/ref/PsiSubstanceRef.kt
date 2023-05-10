package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.SubstanceReferenceFromPsiSubstanceRef
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner

interface PsiSubstanceRef : PsiUIDOwner {
    override fun getReference(): SubstanceReferenceFromPsiSubstanceRef {
        return SubstanceReferenceFromPsiSubstanceRef(this)
    }
}
