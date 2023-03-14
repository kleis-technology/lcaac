package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.QuantityReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.psi.PsiReference

interface PsiQuantityRef : PsiUIDOwner {
    override fun getReference(): PsiReference? {
        return QuantityReference(this)
    }
}
