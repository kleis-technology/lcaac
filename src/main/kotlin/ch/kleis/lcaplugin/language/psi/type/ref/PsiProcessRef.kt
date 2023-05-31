package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.ProcessReferenceFromPsiProcessRef
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.psi.PsiReference

interface PsiProcessRef : PsiUIDOwner {
    override fun getReference(): ProcessReferenceFromPsiProcessRef {
        return ProcessReferenceFromPsiProcessRef(this)
    }
}
