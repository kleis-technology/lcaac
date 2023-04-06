package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.QuantityReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.psi.PsiReference

interface PsiQuantityRef : PsiUIDOwner, PsiLcaRef {
    override fun getReference(): QuantityReference {
        return QuantityReference(this)
    }
}
