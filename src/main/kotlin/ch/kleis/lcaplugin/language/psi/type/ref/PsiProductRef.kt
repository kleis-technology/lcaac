package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.ProductReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.psi.PsiReference

interface PsiProductRef : PsiUIDOwner {
    override fun getReference(): PsiReference? {
        return ProductReference(this)
    }
}
