package ch.kleis.lcaplugin.language.psi.type

import com.intellij.psi.PsiNamedElement

interface PsiSubstanceId : PsiNamedElement {
    fun getSubstance(): PsiUniqueId
    fun getCompartment(): PsiUniqueId?
    fun getSubcompartment(): PsiUniqueId?
}
