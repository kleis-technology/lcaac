package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.DataReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner

interface PsiDataRef : PsiUIDOwner {
    override fun getReference(): DataReference {
        return DataReference(this)
    }
}
