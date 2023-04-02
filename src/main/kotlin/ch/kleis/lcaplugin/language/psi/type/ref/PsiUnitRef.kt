package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.UnitReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner

interface PsiUnitRef : PsiUIDOwner, PsiLcaRef {
    override fun getReference(): UnitReference {
        return UnitReference(this)
    }
}
