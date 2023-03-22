package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.UnitReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.psi.PsiReference

interface PsiUnitRef : PsiUIDOwner {
    override fun getReference(): PsiReference? {
        return UnitReference(this)
    }
}
