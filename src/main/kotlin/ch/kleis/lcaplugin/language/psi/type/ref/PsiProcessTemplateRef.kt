package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.ProcessReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.psi.PsiReference

interface PsiProcessTemplateRef : PsiUIDOwner {
    override fun getReference(): PsiReference? {
        return ProcessReference(this)
    }
}
