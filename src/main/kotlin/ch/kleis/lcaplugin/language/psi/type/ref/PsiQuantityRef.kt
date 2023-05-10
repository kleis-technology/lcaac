package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.QuantityReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner

interface PsiQuantityRef : PsiUIDOwner {
    override fun getReference(): QuantityReference {
        return QuantityReference(this)
    }
}
