package ch.kleis.lcaplugin.language.psi.type.traits

import ch.kleis.lcaplugin.language.psi.type.PsiUnit

interface PsiUnitOwner {
    fun getUnitElement() : PsiUnit
}
