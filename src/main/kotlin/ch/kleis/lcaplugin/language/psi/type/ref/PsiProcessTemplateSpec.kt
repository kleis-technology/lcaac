package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.ProcessReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner

interface PsiProcessTemplateSpec : PsiUIDOwner {
    override fun getReference(): ProcessReference {
        return ProcessReference(this)
    }
}
