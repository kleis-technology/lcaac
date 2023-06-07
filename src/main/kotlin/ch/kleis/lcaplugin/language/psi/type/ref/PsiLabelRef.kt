package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.LabelReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner

interface PsiLabelRef : PsiUIDOwner {
    override fun getReference(): LabelReference {
        return LabelReference(this)
    }
}
