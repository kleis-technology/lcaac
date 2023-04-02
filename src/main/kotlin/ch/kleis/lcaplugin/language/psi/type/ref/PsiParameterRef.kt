package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.ParameterReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner

interface PsiParameterRef : PsiUIDOwner, PsiLcaRef {
    override fun getReference(): ParameterReference {
        return ParameterReference(this)
    }
}
