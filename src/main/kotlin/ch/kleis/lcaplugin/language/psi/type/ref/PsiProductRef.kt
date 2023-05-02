package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.ProductReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner

interface PsiProductRef : PsiUIDOwner {
    override fun getReference(): ProductReference {
        return ProductReference(this)
    }
}
